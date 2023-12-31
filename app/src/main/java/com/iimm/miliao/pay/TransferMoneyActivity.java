package com.iimm.miliao.pay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iimm.miliao.AppConstant;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.EventTransfer;
import com.iimm.miliao.bean.Transfer;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.me.redpacket.ChangePayPasswordActivity;
import com.iimm.miliao.ui.me.redpacket.PayPasswordVerifyDialog;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.view.window.KeyBoad;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * 转账
 */
public class TransferMoneyActivity extends BaseActivity {
    private String mTransferredUserId, mTransferredName;

    private ImageView mTransferredIv;
    private TextView mTransferredTv;

    private String money, words;// 转账金额与转账说明
    private TextView mTransferDescClickTv;
    private EditText mTransferDescTv;

    private EditText et_transfer;
    private TextView money_tv;
    private KeyBoad keyBoad;
    private boolean isUiCreat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_money);
        mTransferredUserId = getIntent().getStringExtra(AppConstant.EXTRA_USER_ID);
        mTransferredName = getIntent().getStringExtra(AppConstant.EXTRA_NICK_NAME);
        initActionBar();
        initView();
        initEvent();
        initKeyBoad();
        checkHasPayPassword();
    }

    private void checkHasPayPassword() {
        boolean hasPayPassword = PreferenceUtils.getBoolean(this, Constants.IS_PAY_PASSWORD_SET + coreManager.getSelf().getUserId(), true);
        if (!hasPayPassword) {
            Intent intent = new Intent(this, ChangePayPasswordActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        ImageView imageView = findViewById(R.id.iv_title_left);
        imageView.setImageResource(R.drawable.icon_close_circle);
        imageView.setOnClickListener(view -> finish());
        TextView titleTv = findViewById(R.id.tv_title_center);
        titleTv.setText(getString(R.string.transfer_money));
    }

    private void initView() {
        mTransferredIv = findViewById(R.id.tm_iv);
        mTransferredTv = findViewById(R.id.tm_tv);
        AvatarHelper.getInstance().displayAvatar(mTransferredUserId, mTransferredIv);
        mTransferredTv.setText(mTransferredName);

        mTransferDescTv = findViewById(R.id.transfer_desc_tv);
        mTransferDescClickTv = findViewById(R.id.transfer_edit_desc_tv);


        et_transfer = findViewById(R.id.et_transfer);
        money_tv = findViewById(R.id.money_tv);
        keyBoad = new KeyBoad(TransferMoneyActivity.this, getWindow().getDecorView(), et_transfer);
    }

    private void initEvent() {
        /*
        mTransferDescClickTv.setOnClickListener(v -> {
            VerifyDialog verifyDialog = new VerifyDialog(mContext);
            verifyDialog.setVerifyClickListener(getString(R.string.transfer_money_desc), getString(R.string.transfer_desc_max_length_10),
                    words, 10, new VerifyDialog.VerifyClickListener() {
                        @Override
                        public void cancel() {

                        }

                        @Override
                        public void send(String str) {
                            words = str;
                            if (TextUtils.isEmpty(words)) {
                                mTransferDescTv.setText("");
                                mTransferDescTv.setVisibility(View.GONE);
                                mTransferDescClickTv.setText(getString(R.string.transfer_money_desc));
                            } else {
                                mTransferDescTv.setText(str);
                                mTransferDescTv.setVisibility(View.VISIBLE);
                                mTransferDescClickTv.setText(getString(R.string.transfer_modify));
                            }
                            keyBoad.show();
                        }

                    });
            verifyDialog.setOkButton(R.string.sure);
            keyBoad.dismiss();
            Window window = verifyDialog.getWindow();

            if (window != null) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); // 软键盘弹起
            }
            verifyDialog.show();
        });*/

//        findViewById(R.id.transfer_btn).setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());
        findViewById(R.id.transfer_btn).setOnClickListener(v -> {
            money = et_transfer.getText().toString().trim();

            if (TextUtils.isEmpty(money) || Double.parseDouble(money) <= 0) {
                Toast.makeText(mContext, getString(R.string.transfer_input_money), Toast.LENGTH_SHORT).show();
                return;
            }

            if (money.endsWith(".")) {
                money = money.replace(".", "");
            }

            PayPasswordVerifyDialog dialog = new PayPasswordVerifyDialog(this);
            dialog.setAction(getString(R.string.transfer_money_to_someone, mTransferredName));
            dialog.setMoney(money);
            words = mTransferDescTv.getText().toString().trim();
            dialog.setOnInputFinishListener(password -> transfer(money, words, password));
            dialog.show();
        });
    }

    private void initKeyBoad() {
        et_transfer.setOnFocusChangeListener((v, hasFocus) -> {
            if (keyBoad != null && isUiCreat) {
                keyBoad.refreshKeyboardOutSideTouchable(!hasFocus);
            } else if (isUiCreat) {
                keyBoad.show();
            }
            if (hasFocus) {
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(et_transfer.getWindowToken(), 0);
            }
        });
        et_transfer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.startsWith(".")) {
                    et_transfer.setText("0" + text);

                } else if (text.startsWith("0") && !text.contains(".") && text.length() > 1) {
                    et_transfer.setText(text.substring(1, text.length()));
                }
                if (TextUtils.isEmpty(et_transfer.getText().toString().trim())) {
                    money_tv.setText("￥0.00");
                } else {
                    money_tv.setText(String.format("￥%s", et_transfer.getText().toString().trim()));
                }


            }
        });

        et_transfer.setOnClickListener(v -> {
            if (keyBoad != null) {
                keyBoad.show();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        isUiCreat = true;
    }

    public void transfer(String money, final String words, String payPassword) {
        if (!ImHelper.checkXmppAuthenticated()) {
            return;
        }
        Map<String, String> params = new HashMap();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("toUserId", mTransferredUserId);
        params.put("money", money);
        params.put("remark", words);

        HttpUtils.get().url(coreManager.getConfig().TRANSFER_SEND_TRANSFER)
                .params(params)
                .addSecret(payPassword, money)
                .build()
                .execute(new BaseCallback<Transfer>(Transfer.class) {

                    @Override
                    public void onResponse(ObjectResult<Transfer> result) {
                        Transfer transfer = result.getData();
                        if (result.getResultCode() != 1) {
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        } else {
                            String objectId = transfer.getId();
                            ChatMessage message = new ChatMessage();
                            message.setType(Constants.TYPE_TRANSFER);
                            message.setFromUserId(coreManager.getSelf().getUserId());
                            message.setFromUserName(coreManager.getSelf().getNickName());
                            message.setToUserId(mTransferredUserId);
                            message.setContent(money);// 转账金额
                            message.setFilePath(words); // 转账说明
                            message.setObjectId(objectId); // 红包id
                            message.setPacketId(ToolUtils.getUUID());
                            message.setDoubleTimeSend(TimeUtils.time_current_time_double());
                            CoreManager.updateMyBalance();

                            EventBus.getDefault().post(new EventTransfer(message));
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }
}
