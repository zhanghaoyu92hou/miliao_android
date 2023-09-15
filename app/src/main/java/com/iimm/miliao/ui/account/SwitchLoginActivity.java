package com.iimm.miliao.ui.account;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Code;
import com.iimm.miliao.bean.LoginRegisterResult;
import com.iimm.miliao.bean.User;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.helper.PrivacySettingHelper;
import com.iimm.miliao.helper.UsernameHelper;
import com.iimm.miliao.ui.MainActivity;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DeviceInfoUtil;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.volley.Result;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;

public class SwitchLoginActivity extends BaseActivity implements View.OnClickListener {
    private EditText auth_code_edit;
    private Button loginBtn;
    private User mLastLoginUser;
    private TextView tv_prefix;
    private int mobilePrefix = 86;
    private int mOldLoginStatus;
    private Button mSendAgainBtn;
    private int reckonTime = 60;
    private String mRandCode;
    private EditText mImageCodeEdit;
    private ImageView mImageCodeIv;
    private ImageView mRefreshIv;
    private TextView mNickNameTv;
    private ImageView mAvatarImgView;
    private String mImageCodeStr;
    private EditText mPhoneNumberEdit;

    private Handler mReckonHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0x1) {
                mSendAgainBtn.setText(reckonTime + " " + "S" + "重新获取");
                mSendAgainBtn.setAlpha(0.5f);
                reckonTime--;
                if (reckonTime < 0) {
                    mReckonHandler.sendEmptyMessage(0x2);
                } else {
                    mReckonHandler.sendEmptyMessageDelayed(0x1, 1000);
                }
            } else if (msg.what == 0x2) {
                // 60秒结束
                mSendAgainBtn.setAlpha(1f);
                mSendAgainBtn.setText(R.string.get_msg_code);
                mSendAgainBtn.setEnabled(true);
                reckonTime = 60;
            }
        }
    };
    private String phone;
    private int type;

    public SwitchLoginActivity() {
        noLoginRequired();
    }

    public static void bindThird(Context ctx) {
        Intent intent = new Intent(ctx, SwitchLoginActivity.class);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);
        PreferenceUtils.putBoolean(this, Constants.LOGIN_CONFLICT, false);// 重置登录冲突记录

        mOldLoginStatus = MyApplication.getInstance().mUserStatus;
        initActionBar();
        initView();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.verification_code) + getString(R.string.login));
        TextView tvRight = (TextView) findViewById(R.id.tv_title_right);
        tvRight.setVisibility(View.GONE);
    }

    private void initView() {
        mPhoneNumberEdit = findViewById(R.id.phone_numer_edit);
        tv_prefix = (TextView) findViewById(R.id.tv_prefix);
        if (coreManager.getConfig().registerUsername == 0) {
            tv_prefix.setVisibility(View.GONE);
        } else {
            tv_prefix.setOnClickListener(this);
        }
        mImageCodeEdit = (EditText) findViewById(R.id.image_tv);
        mImageCodeIv = (ImageView) findViewById(R.id.image_iv);
        mRefreshIv = (ImageView) findViewById(R.id.image_iv_refresh);
        mSendAgainBtn = (Button) findViewById(R.id.send_again_btn);
        auth_code_edit = findViewById(R.id.auth_code_edit);
        loginBtn = (Button) findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(this);

        UsernameHelper.initEditText(mPhoneNumberEdit, coreManager.getConfig().registerUsername);

        findViewById(R.id.main_content).setOnClickListener(this);

        mRefreshIv.setOnClickListener(new View.OnClickListener() {// 刷新图形码
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mPhoneNumberEdit.getText().toString().trim())) {
                    ToastUtil.showToast(mContext, "手机号为空,不能刷新图形验证码");
                } else {
                    requestImageCode();
                }
            }
        });
        mSendAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone = mPhoneNumberEdit.getText().toString().trim();
                mImageCodeStr = mImageCodeEdit.getText().toString().trim();
                if (TextUtils.isEmpty(mImageCodeStr)) {
                    ToastUtil.showToast(mContext, getString(R.string.tip_verification_code_empty));
                    return;
                }
                // 验证手机号是否注册
                verifyPhoneIsRegistered(phone, mImageCodeStr);

            }
        });

        mPhoneNumberEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // 注册页面手机号输入完成后自动刷新验证码，
                    // 只在移开焦点，也就是点击其他EditText时调用，
                    requestImageCode();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_prefix:
                // 选择国家区号
                Intent intent = new Intent(this, SelectPrefixActivity.class);
                startActivityForResult(intent, SelectPrefixActivity.REQUEST_MOBILE_PREFIX_LOGIN);
                break;
            case R.id.login_btn:
                if (TextUtils.isEmpty(phone)) {
                    ToastUtil.showToast(mContext, "手机号不能为空");
                    return;
                }
                if (TextUtils.isEmpty(mRandCode)) {
                    ToastUtil.showToast(mContext, "验证码不能为空");
                    return;
                }
                if (mRandCode.equals(auth_code_edit.getText().toString().trim())) {
                    Log.e("zx", "onClick: " + "login_btn");
                    login(false);
                } else {
                    ToastUtil.showToast(SwitchLoginActivity.this, "验证码错误");
                }
                break;
            case R.id.register_account_btn:
                startActivity(new Intent(SwitchLoginActivity.this, RegisterActivity.class));
                break;
            case R.id.forget_password_btn:
                startActivity(new Intent(SwitchLoginActivity.this, FindPwdActivity.class));
                break;
            case R.id.main_content:
                // 点击空白区域隐藏软键盘
                InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (inputManager != null) {
                    inputManager.hideSoftInputFromWindow(findViewById(R.id.main_content).getWindowToken(), 0); //强制隐藏键盘
                }
                break;
        }
    }

    /**
     * 请求验证码
     */
    private void requestAuthCode(String phoneStr, String imageCodeStr) {
        Map<String, String> params = new HashMap<>();
        String language = Locale.getDefault().getLanguage();
        params.put("language", language);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("telephone", phoneStr);
        Log.e("zx", "requestAuthCode: " + phoneStr);
        params.put("imgCode", imageCodeStr);
        params.put("isRegister", String.valueOf(0));
        params.put("version", "1");

        DialogHelper.showDefaulteMessageProgressDialog(this);
        Log.e("zx", "requestAuthCode: " + imageCodeStr);
        HttpUtils.get().url(coreManager.getConfig().SEND_AUTH_CODE)
                .params(params)
                .build()
                .execute(new BaseCallback<Code>(Code.class) {

                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            Log.e("zx", "onResponse: " + result.getData().getCode());
                            mSendAgainBtn.setEnabled(false);
                            mRandCode = result.getData().getCode();// 记录验证码
                            // 开始倒计时
                            mReckonHandler.sendEmptyMessage(0x1);
                        } else {
                            if (!TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(SwitchLoginActivity.this,
                                        result.getResultMsg());
                            } else {
                                ToastUtil.showToast(SwitchLoginActivity.this,
                                        getString(R.string.tip_server_error));
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(mContext);
                    }
                });
    }

    /**
     * 验证手机是否注册
     */
    private void verifyPhoneIsRegistered(final String phoneStr, final String imageCodeStr) {
        verifyPhoneNumber(phoneStr, new Runnable() {
            @Override
            public void run() {
                requestAuthCode(phoneStr, imageCodeStr);
            }
        });
    }

    /**
     * 请求图形验证码
     */
    private void requestImageCode() {
        String url = coreManager.getConfig().USER_GETCODE_IMAGE + "?telephone=" + mobilePrefix + mPhoneNumberEdit.getText().toString().trim();

        Log.d("zx", "requestImageCode: " + url);
        Glide.with(mContext).load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        mImageCodeIv.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        ToastUtil.showToast(SwitchLoginActivity.this, R.string.tip_verification_code_load_failed);
                    }
                });
    }

    private void verifyPhoneNumber(String phoneNumber, final Runnable onSuccess) {
        Map<String, String> params = new HashMap<>();
        params.put("telephone", phoneNumber);
        params.put("areaCode", "" + mobilePrefix);
        params.put("verifyType", "1");
        params.put("serial", DeviceInfoUtil.getDeviceId(mContext));
        HttpUtils.get().url(coreManager.getConfig().VERIFY_TELEPHONE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result == null) {
                            ToastUtil.showToast(SwitchLoginActivity.this,
                                    R.string.data_exception);
                            return;
                        }

                        if (result.getResultCode() == 1) {
                            onSuccess.run();
                        } else {
                            requestImageCode();
                            // 手机号已经被注册
                            if (!TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(SwitchLoginActivity.this,
                                        result.getResultMsg());
                            } else {
                                ToastUtil.showToast(SwitchLoginActivity.this,
                                        R.string.tip_server_error);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(SwitchLoginActivity.this);
                    }
                });
    }

    private void login(boolean third) {
        PreferenceUtils.putInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        if (TextUtils.isEmpty(mRandCode)) {
            return;
        }
        final String phoneNumber = mPhoneNumberEdit.getText().toString().trim();
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HashMap<String, String> params = new HashMap<>();
        // 得到的为8618720966659,服务器却需要18720966659
        // 去掉区号,
        String sPrefix = String.valueOf(mobilePrefix);
        String phoneNumberRel;
        if (phoneNumber.startsWith(sPrefix)) {
            phoneNumberRel = phoneNumber.substring(sPrefix.length());
        } else {
            phoneNumberRel = phoneNumber;
        }
        params.put("telephone", Md5Util.toMD5(phoneNumberRel));// 账号登陆的时候需要MD5以下，服务器需求
        params.put("verificationCode", mRandCode);
        //params.put("imgCode", mImageCodeStr);
        Log.e("zx", "login: " + mRandCode);
        params.put("xmppVersion", "1");
        params.put("areaCode", String.valueOf(mobilePrefix));
        // 附加信息
        params.put("model", DeviceInfoUtil.getModel());
        params.put("osVersion", DeviceInfoUtil.getOsVersion());
        params.put("serial", DeviceInfoUtil.getDeviceId(mContext));
        params.put("loginType", "1");//验证码登录

        // 地址信息
        double latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
        double longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();
        if (latitude != 0)
            params.put("latitude", String.valueOf(latitude));
        if (longitude != 0)
            params.put("longitude", String.valueOf(longitude));

        if (MyApplication.IS_OPEN_CLUSTER) {// 服务端集群需要
            String area = PreferenceUtils.getString(this, AppConstant.EXTRA_CLUSTER_AREA);
            if (!TextUtils.isEmpty(area)) {
                params.put("area", area);
            }
        }
        String url = coreManager.getConfig().USER_LOGIN;
        HttpUtils.get().url(url)
                .params(params)
                .build()
                .execute(new BaseCallback<LoginRegisterResult>(LoginRegisterResult.class) {

                    @Override
                    public void onResponse(ObjectResult<LoginRegisterResult> result) {

                        if (result == null) {
                            DialogHelper.dismissProgressDialog();
                            ToastUtil.showErrorData(mContext);
                            return;
                        }
                        boolean success = false;
                        if (result.getResultCode() == Result.CODE_SUCCESS) {
                            success = LoginHelper.setLoginUser(mContext, coreManager, phone, result.getData().getPassword(), result);// 设置登陆用户信息
                        }

                        if (success) {
                            LoginRegisterResult.Settings settings = result.getData().getSettings();
                            MyApplication.getInstance().initPayPassword(result.getData().getUserId(), result.getData().getPayPassword());
                            PrivacySettingHelper.setPrivacySettings(SwitchLoginActivity.this, settings);
                            MyApplication.getInstance().initMulti();

                            // 登陆成功
                            LoginRegisterResult.Login login = result.getData().getLogin();
                            if (login != null && login.getSerial() != null && login.getSerial().equals(DeviceInfoUtil.getDeviceId(mContext))
                                    && mOldLoginStatus != LoginHelper.STATUS_USER_NO_UPDATE && mOldLoginStatus != LoginHelper.STATUS_NO_USER) {
                                // 如果Token没变，上次更新也是完整更新，那么直接进入Main程序
                                // 其他的登陆地方都需进入DataDownloadActivity，在DataDownloadActivity里发送此广播
                                LoginHelper.broadcastLogin(SwitchLoginActivity.this);
                                // Intent intent = new Intent(mContext, MainActivity.class);
                                Intent intent = new Intent(SwitchLoginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                // 否则，进入数据下载界面
                                // startActivity(new Intent(SwitchLoginActivity.this, DataDownloadActivity.class));
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

    private void register() {
        RegisterActivity.registerFromThird(
                this,
                mobilePrefix,
                mPhoneNumberEdit.getText().toString(),
                null,
                type
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != SelectPrefixActivity.RESULT_MOBILE_PREFIX_SUCCESS)
            return;
        mobilePrefix = data.getIntExtra(Constants.MOBILE_PREFIX, 86);
        tv_prefix.setText("+" + mobilePrefix);
    }
}
