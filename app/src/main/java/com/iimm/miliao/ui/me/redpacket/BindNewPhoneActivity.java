package com.iimm.miliao.ui.me.redpacket;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.db.dao.UserDao;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.account.SelectPrefixActivity;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.CommonalityTools;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.view.NoDoubleClickListener;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import okhttp3.Call;

public class BindNewPhoneActivity extends BaseActivity {
    private TextView mAreaCodeTv;
    private EditText mPhoneEt;
    private Button mConfirmBt;
    private LinearLayout mCodeLl;
    private LinearLayout mVerificationLl;
    private LinearLayout password;
    private LinearLayout password_confirm;
    private EditText mGraphicCodeEt;
    private EditText mVerificationCodeEt;
    private Button mVerificationCodeBt;
    private ImageView mGraphicCodeIv;
    private String type = "1";
    private String phoneNum = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bindnewphone);
        initActionBar();
        initView();
        initClick();
    }

    private void initClick() {
        mPhoneEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                phoneNum = mPhoneEt.getText().toString().trim();
                requestImageCode();
            }
        });
        mAreaCodeTv.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                // 选择国家区号
                Intent intent = new Intent(BindNewPhoneActivity.this, SelectPrefixActivity.class);
                BindNewPhoneActivity.this.startActivityForResult(intent, SelectPrefixActivity.REQUEST_MOBILE_PREFIX_LOGIN);
            }
        });
        mGraphicCodeIv.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                CommonalityTools.requestImageCode(BindNewPhoneActivity.this, coreManager, String.format("%s%s", mAreaCodeTv.getText().toString().trim().substring(1, mAreaCodeTv.getText().toString().trim().length()), mPhoneEt.getText().toString().trim()), mGraphicCodeIv);
            }
        });
        mVerificationCodeBt.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                if (BindNewPhoneActivity.this.verificationCode()) {
                    String trim = mPhoneEt.getText().toString().trim();
                    if (!phoneNum.equals(trim)) {
                        ToastUtil.showToast(R.string.verification_code_has_expired);
                        return;
                    }
                    CommonalityTools.getCode(BindNewPhoneActivity.this, mPhoneEt.getText().toString().trim(), mGraphicCodeEt.getText().toString()
                            , coreManager, mVerificationCodeBt,mAreaCodeTv.getText().toString().trim().substring(1, mAreaCodeTv.getText().toString().trim().length()));
                }
            }
        });
        mConfirmBt.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                if (BindNewPhoneActivity.this.verification()) {
                    BindNewPhoneActivity.this.confirm();
                }
            }
        });
    }

    private boolean verificationCode() {
        if (TextUtils.isEmpty(mPhoneEt.getText().toString().trim())) {
            ToastUtil.showToast(BindNewPhoneActivity.this, getString(R.string.hint_input_phone_number));
            return false;
        } else if (TextUtils.isEmpty(mGraphicCodeEt.getText().toString().trim())) {
            ToastUtil.showToast(BindNewPhoneActivity.this, getString(R.string.tip_verification_code_empty));
            return false;
        }
        return true;
    }

    private void confirm() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        String phone = mAreaCodeTv.getText().toString().trim().substring(1, mAreaCodeTv.getText().toString().trim().length());

        HttpUtils.get().url(coreManager.getConfig().OTHER_BIND_Phone_Pass_Word)
                .params("access_token", coreManager.getSelfStatus().accessToken)
                .params("telePhone", mPhoneEt.getText().toString().trim())
                .params("areaCode", phone)
                .params("loginType", type)
                .params("smsCode", mVerificationCodeEt.getText().toString().trim())
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            if (coreManager.getSelf() != null) {
                                coreManager.getSelf().setTelephone(phone + mPhoneEt.getText().toString().trim());
                                coreManager.getSelf().setPhone(mPhoneEt.getText().toString().trim());
                                UserDao.getInstance().updateTelephone(coreManager.getSelf().getUserId(), phone + mPhoneEt.getText().toString().trim());
                                UserDao.getInstance().updatePhone(coreManager.getSelf().getUserId(), mPhoneEt.getText().toString().trim());
                            }
                            setResult(RESULT_OK);
                            finish();
                            ToastUtil.showToast(BindNewPhoneActivity.this, result.getResultMsg());
                        } else {
                            ToastUtil.showToast(BindNewPhoneActivity.this, result.getResultMsg());
                        }

                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(BindNewPhoneActivity.this, e.getMessage());
                    }
                });
    }

    private void requestImageCode() {
        if (TextUtils.isEmpty(mPhoneEt.getText().toString())) {
            ToastUtil.showToast(mContext, getString(R.string.tip_no_phone_number_get_v_code));
            return;
        }
        CommonalityTools.requestImageCode(BindNewPhoneActivity.this, coreManager, String.format("%s%s", mAreaCodeTv.getText().toString().trim().substring(1, mAreaCodeTv.getText().toString().trim().length()), mPhoneEt.getText().toString().trim()), mGraphicCodeIv);
    }

    private boolean verification() {
        if (TextUtils.isEmpty(mPhoneEt.getText().toString().trim())) {
            ToastUtil.showToast(BindNewPhoneActivity.this, getString(R.string.hint_input_phone_number));
            return false;
        } else if (PreferenceUtils.getInt(BindNewPhoneActivity.this, AppConstant.LOGIN_TYPE) == 0) {
            if (TextUtils.isEmpty(mVerificationCodeEt.getText().toString().trim())) {
                ToastUtil.showToast(BindNewPhoneActivity.this, getString(R.string.please_input_auth_code));
                return false;
            }
        }
        return true;
    }

    private void initView() {
        mAreaCodeTv = findViewById(R.id.tv_prefix);
        mPhoneEt = findViewById(R.id.phone_numer_edit);
        mConfirmBt = findViewById(R.id.confirm_btn);
        mCodeLl = findViewById(R.id.change_phone_codell_iv);
        mVerificationLl = findViewById(R.id.change_phone_verification_code_ll);
        mGraphicCodeEt = findViewById(R.id.change_phone_graphic_code_et);
        mVerificationCodeEt = findViewById(R.id.change_phone_verification_code_et);
        mVerificationCodeBt = findViewById(R.id.change_phone_verification_code_bt);
        mGraphicCodeIv = findViewById(R.id.change_phone_graphic_code_iv);


    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.set_the_phone_number));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == SelectPrefixActivity.RESULT_MOBILE_PREFIX_SUCCESS && data != null) {
            int mobilePrefix = data.getIntExtra(Constants.MOBILE_PREFIX, 86);
            mAreaCodeTv.setText(String.format("+%d", mobilePrefix));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonalityTools.closeTimer();
    }
}