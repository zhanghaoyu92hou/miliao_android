package com.iimm.miliao.ui.account;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.adapter.MessageLogin;
import com.iimm.miliao.bean.LoginRegisterResult;
import com.iimm.miliao.bean.LoginRegisterResult.Login;
import com.iimm.miliao.bean.User;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.UserDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.helper.PasswordHelper;
import com.iimm.miliao.helper.PrivacySettingHelper;
import com.iimm.miliao.sp.UserSp;
import com.iimm.miliao.ui.MainActivity;
import com.iimm.miliao.ui.base.ActivityStack;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DeviceInfoUtil;
import com.iimm.miliao.util.EventBusHelper;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.UiUtils;
import com.iimm.miliao.volley.Result;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;

import java.util.HashMap;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 历史登陆界面
 */

public class LoginHistoryActivity extends BaseActivity implements View.OnClickListener {
    private RoundedImageView mAvatarImgView;
    private TextView mNickNameTv;
    private EditText mPasswordEdit;
    private int mobilePrefix = 86;
    private User mLastLoginUser;
    private int mOldLoginStatus;

    public LoginHistoryActivity() {
        noLoginRequired();
    }

    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, LoginHistoryActivity.class);
        // 清空activity栈，
        // 重建期间白屏，暂且放弃，
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_history);
        PreferenceUtils.putBoolean(this, Constants.LOGIN_CONFLICT, false);// 重置登录冲突记录
        String userId = UserSp.getInstance(this).getUserId("");
        mLastLoginUser = UserDao.getInstance().getUserByUserId(userId);
        mOldLoginStatus = MyApplication.getInstance().mUserStatus;
        if (!LoginHelper.isUserValidationThirdParty(mLastLoginUser)) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        TextView tv1 = (TextView) findViewById(R.id.tv_title_left);
        TextView tv2 = (TextView) findViewById(R.id.tv_title_right);
        tv1.setText(R.string.app_name);
        tv2.setText(R.string.switch_account);
        tv1.setVisibility(View.GONE);
        tv2.setVisibility(View.GONE);
        TextView tvTitleCenter = findViewById(R.id.tv_title_center);
        tvTitleCenter.setText(getResources().getString(R.string.login));
        tvTitleCenter.setVisibility(View.VISIBLE);
        findViewById(R.id.b_switch_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginHistoryActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        initView();
        EventBusHelper.register(this);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageLogin message) {
        finish();
    }

    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        switch (v.getId()) {
            case R.id.login_btn:
                login();
                break;
            case R.id.register_account_btn:
                int type = PreferenceUtils.getInt(this, AppConstant.LOGIN_TYPE, 0);
                RegisterActivity.registerFromThird(this, 86, "", "", type);
                break;
            case R.id.forget_password_btn:
                Intent intent = new Intent(this, FindPwdActivity.class);
                intent.putExtra("type", PreferenceUtils.getInt(this, AppConstant.LOGIN_TYPE, 0));
                startActivity(intent);
                break;
        }
    }

    private void initView() {
        mAvatarImgView = (RoundedImageView) findViewById(R.id.avatar_img);
        mNickNameTv = (TextView) findViewById(R.id.nick_name_tv);
        mPasswordEdit = (EditText) findViewById(R.id.password_edit);
        PasswordHelper.bindPasswordEye(mPasswordEdit, findViewById(R.id.tbEye));
        mobilePrefix = PreferenceUtils.getInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        Button loginBtn, registerBtn, forgetPasswordBtn;
        loginBtn = (Button) findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(this);
        registerBtn = (Button) findViewById(R.id.register_account_btn);
        registerBtn.setOnClickListener(this);
        if (coreManager.getConfig().isOpenRegister) {
            registerBtn.setVisibility(View.VISIBLE);
        } else {
            registerBtn.setVisibility(View.GONE);
        }
        forgetPasswordBtn = (Button) findViewById(R.id.forget_password_btn);
        if (PreferenceUtils.getInt(LoginHistoryActivity.this, AppConstant.LOGIN_TYPE) == 1 && !coreManager.getConfig().isQestionOpen) {
            forgetPasswordBtn.setVisibility(View.GONE);
        } else {
            forgetPasswordBtn.setOnClickListener(this);
        }
/*
        registerBtn.setTextColor(SkinUtils.getSkin(this).getAccentColor());
        forgetPasswordBtn.setTextColor(SkinUtils.getSkin(this).getAccentColor());
*/

        // mPasswordEdit.setHint(InternationalizationHelper.getString("JX_InputPassWord"));
        loginBtn.setText(InternationalizationHelper.getString("JX_Login"));
        //registerBtn.setText(InternationalizationHelper.getString("JX_Register"));
//        forgetPasswordBtn.setText(InternationalizationHelper.getString("JX_ForgetPassWord"));

        AvatarHelper.getInstance().displayRoundAvatar(mLastLoginUser.getNickName(), mLastLoginUser.getUserId(), mAvatarImgView, true);
        mNickNameTv.setText(mLastLoginUser.getNickName());
    }

    private void login() {
        PreferenceUtils.putInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        String password = mPasswordEdit.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            return;
        }
        final String digestPwd = new String(Md5Util.toMD5(password));

        DialogHelper.showDefaulteMessageProgressDialog(this);
        HashMap<String, String> params = new HashMap<>();
        // 得到的为8618720966659,服务器却需要18720966659
        String phoneNumber = mLastLoginUser.getTelephone();
        // 去掉区号,
        String sPrefix = String.valueOf(mobilePrefix);
        String phoneNumberRel;
        if (phoneNumber.startsWith(sPrefix)) {
            phoneNumberRel = phoneNumber.substring(sPrefix.length());
        } else {
            phoneNumberRel = phoneNumber;
        }
        params.put("appBrand", DeviceInfoUtil.getBrand());//获取手机品牌
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("telephone", Md5Util.toMD5(phoneNumberRel));// 账号登陆的时候需要MD5以下，服务器需求
        params.put("password", digestPwd);// 账号登陆的时候需要MD5以下，服务器需求
        params.put("xmppVersion", "1");
        // 附加信息
        params.put("model", DeviceInfoUtil.getModel());
        params.put("osVersion", DeviceInfoUtil.getOsVersion());
        params.put("serial", DeviceInfoUtil.getDeviceId(mContext));  //手机 IMEI 串号
        if (!coreManager.getConfig().disableLocationServer) {
            // 地址信息
            double latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
            double longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();
            if (latitude != 0)
                params.put("latitude", String.valueOf(latitude));
            if (longitude != 0)
                params.put("longitude", String.valueOf(longitude));
        }
        if (MyApplication.IS_OPEN_CLUSTER) {// 服务端集群需要
            String area = PreferenceUtils.getString(this, AppConstant.EXTRA_CLUSTER_AREA);
            if (!TextUtils.isEmpty(area)) {
                params.put("area", area);
            }
        }
        HttpUtils.get().url(coreManager.getConfig().USER_LOGIN)
                .params(params)
                .build()
                .execute(new BaseCallback<LoginRegisterResult>(LoginRegisterResult.class) {

                    @Override
                    public void onResponse(com.xuan.xuanhttplibrary.okhttp.result.ObjectResult<LoginRegisterResult> result) {
                        if (result == null) {
                            DialogHelper.dismissProgressDialog();
                            ToastUtil.showErrorData(mContext);
                            return;
                        }
                        boolean success = false;
                        if (result.getResultCode() == Result.CODE_SUCCESS) {
                            success = LoginHelper.setLoginUser(mContext, coreManager, mLastLoginUser.getTelephone(), digestPwd, result);// 设置登陆用户信息
                        }

                        if (success) {
                            PreferenceUtils.putBoolean(mContext, AppConstant.LOGINSTATU, true);
                            LoginRegisterResult.Settings settings = result.getData().getSettings();
                            MyApplication.getInstance().initPayPassword(result.getData().getUserId(), result.getData().getPayPassword());
                            PrivacySettingHelper.setPrivacySettings(LoginHistoryActivity.this, settings);
                            MyApplication.getInstance().initMulti();

                            // 登陆成功
                            Login login = result.getData().getLogin();
                            if (login != null && login.getSerial() != null && login.getSerial().equals(DeviceInfoUtil.getDeviceId(mContext))
                                    && mOldLoginStatus != LoginHelper.STATUS_USER_NO_UPDATE && mOldLoginStatus != LoginHelper.STATUS_NO_USER) {
                                // 如果Token没变，上次更新也是完整更新，那么直接进入Main程序
                                // 其他的登陆地方都需进入DataDownloadActivity，在DataDownloadActivity里发送此广播
                                LoginHelper.broadcastLogin(LoginHistoryActivity.this);
                                // Intent intent = new Intent(mContext, MainActivity.class);
                                Intent intent = new Intent(LoginHistoryActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                // 否则，进入数据下载界面
                                // startActivity(new Intent(LoginHistoryActivity.this, DataDownloadActivity.class));
                                DataDownloadActivity.start(mContext, result.getData().getIsupdate());
                            }
                            finish();
                        } else {
                            // 登录失败
                            String message = TextUtils.isEmpty(result.getResultMsg()) ? getString(R.string.login_failed) : result.getResultMsg();
                            ToastUtil.showToast(mContext, message);
                        }
                        DialogHelper.dismissProgressDialog();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        ActivityStack.getInstance().exit();
    }
}
