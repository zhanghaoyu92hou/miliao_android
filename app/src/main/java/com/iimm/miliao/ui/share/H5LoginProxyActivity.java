package com.iimm.miliao.ui.share;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.ui.SplashActivity;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.LogUtils;
import com.iimm.miliao.util.PreferenceUtils;

/**
 * 用于h5拉起app登录，
 */
public class H5LoginProxyActivity extends BaseActivity {

    public H5LoginProxyActivity() {
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h5_login_proxy);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Intent intent = getIntent();
        LogUtils.log(TAG, intent);

        // 判断本地登录状态
        int userStatus = LoginHelper.prepareUser(mContext, coreManager);
        boolean isNeedExecuteLogin = false;
        switch (userStatus) {
            case LoginHelper.STATUS_USER_FULL:
            case LoginHelper.STATUS_USER_NO_UPDATE:
            case LoginHelper.STATUS_USER_TOKEN_OVERDUE:
                boolean isConflict = PreferenceUtils.getBoolean(this, Constants.LOGIN_CONFLICT, false);
                if (isConflict) {
                    isNeedExecuteLogin = true;
                }
                break;
            case LoginHelper.STATUS_USER_SIMPLE_TELPHONE:
                isNeedExecuteLogin = true;
                break;
            case LoginHelper.STATUS_NO_USER:
            default:
                isNeedExecuteLogin = true;
        }

        if (isNeedExecuteLogin) {// 需要先执行登录操作
            startActivity(new Intent(mContext, SplashActivity.class));
            finish();
            return;
        }

        if (intent.getData() != null) {
            try {
                Uri data = intent.getData();
                for (String key : data.getQueryParameterNames()) {
                    String value = data.getQueryParameter(key);
                    // 参数统一存在intent.extras里，有的推送不支持，所以要提前处理一下，
                    intent.putExtra(key, value);
                }
            } catch (Exception e) {
                Reporter.post("H5登录intent.data解析失败", e);
            }
        }

        String callback = intent.getStringExtra("callback");
        H5LoginActivity.start(this, callback);

        finish();
    }
}
