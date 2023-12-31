package com.iimm.miliao.ui.me.redpacket.alipay;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.alipay.sdk.app.AuthTask;
import com.alipay.sdk.app.PayTask;
import com.iimm.miliao.AppConfig;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.WXUploadResult;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.me.redpacket.PayPasswordVerifyDialog;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;

public class AlipayHelper {
    private static final String TAG = "AlipayHelper";

    /**
     * 拉起支付宝充值，
     */
    public static void recharge(Activity activity, CoreManager coreManager, String money) {// 调用服务端接口，由服务端统一下单
        DialogHelper.showDefaulteMessageProgressDialog(activity);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("price", money);
        params.put("payType", "1");// 支付方式 1.支付宝 2.微信

        HttpUtils.get().url(coreManager.getConfig().VX_RECHARGE)
                .params(params)
                .build()
                .execute(new BaseCallback<SignResult>(SignResult.class) {

                    @Override
                    public void onResponse(ObjectResult<SignResult> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(activity, result)) {
                            String orderInfo = result.getData().getOrderInfo();
                            Log.i(TAG, "onResponse: orderInfo = " + orderInfo);
                            callAlipay(activity, orderInfo);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(activity);
                    }
                });
    }

    /**
     * 通知后台提现，
     */
    public static void withdraw(Activity activity, CoreManager coreManager, String money, String userId) {
        PayPasswordVerifyDialog dialog = new PayPasswordVerifyDialog(activity, money);
        dialog.setAction(activity.getString(R.string.withdraw));
        // amount单位是分，而且是整元乘100得到的分，一定可以被100整除，
        dialog.setOnInputFinishListener(new PayPasswordVerifyDialog.OnInputFinishListener() {
            @Override
            public void onInputFinish(String password) {
                DialogHelper.showDefaulteMessageProgressDialog(activity);
                String mAccessToken = coreManager.getSelfStatus().accessToken;
                String mLoginUserId = coreManager.getSelf().getUserId();
                String time = String.valueOf(TimeUtils.time_current_time());

                String str1 = AppConfig.apiKey + userId + mLoginUserId;
                String str2 = Md5Util.toMD5(mAccessToken + money + time);
                String str3 = Md5Util.toMD5(password);
                String secret = Md5Util.toMD5(str1 + str2 + str3);
                Log.d(HttpUtils.TAG, String.format(Locale.CHINA, "addSecret: md5(%s+%s+%s+md5(%s+%s+%s)+md5(%s)) = %s", AppConfig.apiKey, userId, mLoginUserId, mAccessToken, money, time, password, secret));

                final Map<String, String> params = new HashMap<>();
                params.put("access_token", mAccessToken);
                params.put("amount", money);
                params.put("time", time);
                params.put("secret", secret);

                HttpUtils.post().url(coreManager.getConfig().ALIPAY_TRANSFER)
                        .params(params)
                        .build()
                        .execute(new BaseCallback<WXUploadResult>(WXUploadResult.class) {

                            @Override
                            public void onResponse(ObjectResult<WXUploadResult> result) {
                                DialogHelper.dismissProgressDialog();
                                if (Result.checkSuccess(activity, result)) {
                                    ToastUtil.showToast(activity, R.string.tip_withdraw_success);
                                    activity.finish();
                                }
                            }

                            @Override
                            public void onError(Call call, Exception e) {
                                DialogHelper.dismissProgressDialog();
                                ToastUtil.showErrorData(activity);
                            }
                        });
            }
        });
        dialog.show();
    }

    /**
     * 拉起支付宝授权，
     */
    public static void auth(Activity activity, CoreManager coreManager, AsyncUtils.Function<String> callback) {
        DialogHelper.showDefaulteMessageProgressDialog(activity);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        HttpUtils.get().url(coreManager.getConfig().ALIPAY_AUTH)
                .params(params)
                .build()
                .execute(new BaseCallback<AuthInfoResult>(AuthInfoResult.class) {

                    @Override
                    public void onResponse(ObjectResult<AuthInfoResult> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(activity, result)) {
                            String userId = result.getData().getAliUserId();
                            if (TextUtils.isEmpty(userId)) {
                                String authInfo = result.getData().getAuthInfo();
                                Log.i(TAG, "onResponse: authInfo = " + authInfo);
                                callAuth(activity, coreManager, authInfo, callback);
                            } else {
                                Log.i(TAG, "onResponse: userId = " + userId);
                                try {
                                    callback.apply(userId);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(activity);
                    }
                });
    }

    private static void callAlipay(Activity activity, String orderInfo) {
        AsyncUtils.doAsync(activity, t -> {
            Reporter.post("拉起支付宝失败，", t);
            AsyncUtils.runOnUiThread(activity, ctx -> {
                ToastUtil.showToast(ctx, R.string.tip_alipay_failed);
            });
        }, c -> {
            PayTask alipay = new PayTask(c.getRef());
            PayResult aliResult = new PayResult(alipay.payV2(orderInfo, true));
            Log.i(TAG, "onResponse: aliResult = " + aliResult);
            c.uiThread(r -> {
                if (TextUtils.isEmpty(aliResult.getMemo())) {
                    ToastUtil.showToast(r, R.string.recharge_success);
                } else {
                    ToastUtil.showToast(r, aliResult.getMemo());
                }
            });
        });
    }

    private static void callAuth(Activity activity, CoreManager coreManager, String authInfo, AsyncUtils.Function<String> callback) {
        AsyncUtils.doAsync(activity, t -> {
            Reporter.post("拉起支付宝失败，", t);
            AsyncUtils.runOnUiThread(activity, ctx -> {
                ToastUtil.showToast(ctx, R.string.tip_alipay_failed);
            });
        }, c -> {
            AuthTask authTask = new AuthTask(activity);
            // 调用授权接口，获取授权结果
            AuthResult aliResult = new AuthResult(authTask.authV2(authInfo, true), true);
            Log.i(TAG, "onResponse: aliResult = " + aliResult);
            c.uiThread(r -> {
                if (TextUtils.equals(aliResult.getResultStatus(), "9000") && TextUtils.equals(aliResult.getResultCode(), "200")) {
                    bindUserId(activity, coreManager, aliResult.getUserId(), callback);
                } else {
                    if (TextUtils.isEmpty(aliResult.getMemo())) {
                        ToastUtil.showToast(r, R.string.tip_alipay_auth_failed);
                    } else {
                        ToastUtil.showToast(r, aliResult.getMemo());
                    }
                }
            });
        });
    }

    /**
     * 上传userId以绑定用户支付宝账号，
     */
    private static void bindUserId(Activity activity, CoreManager coreManager, String userId, AsyncUtils.Function<String> callback) {
        DialogHelper.showDefaulteMessageProgressDialog(activity);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("aliUserId", userId);

        HttpUtils.get().url(coreManager.getConfig().ALIPAY_BIND)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(activity, result)) {
                            try {
                                callback.apply(userId);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(activity);
                    }
                });
    }


    @SuppressWarnings("unused")
    public static void order(Activity ctx, String amount, String appId, String rsa2Private, String rsaPrivate) {
        /*
         * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
         * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
         * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
         *
         * orderInfo 的获取必须来自服务端；
         */
        boolean rsa2 = (rsa2Private.length() > 0);
        Map<String, String> params = OrderInfoUtil2_0.buildOrderParamMap(amount, appId, rsa2);
        String orderParam = OrderInfoUtil2_0.buildOrderParam(params);

        String privateKey = rsa2 ? rsa2Private : rsaPrivate;
        String sign = OrderInfoUtil2_0.getSign(params, privateKey, rsa2);
        final String orderInfo = orderParam + "&" + sign;

        AsyncUtils.doAsync(ctx, c -> {
            PayTask alipay = new PayTask(ctx);
            Map<String, String> result = alipay.payV2(orderInfo, true);
            Log.i("msp", result.toString());
        });
    }
}
