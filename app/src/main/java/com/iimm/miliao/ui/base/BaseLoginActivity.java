package com.iimm.miliao.ui.base;

import android.os.Bundle;
import android.util.Log;

import com.iimm.miliao.AppConfig;
import com.iimm.miliao.bean.User;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseLoginActivity extends ActionBackActivity implements CoreStatusListener {
    public CoreManager coreManager;
    private List<CoreStatusListener> coreStatusListeners;
    private boolean loginRequired = true;
    private boolean configRequired = true;

    protected void noLoginRequired() {
        Log.d(TAG, "noLoginRequired() called");
        loginRequired = false;
    }

    protected void noConfigRequired() {
        Log.d(TAG, "noConfigRequired() called");
        configRequired = false;
    }

    // 注册CoreManager初始化状态的监听，比如fragment可以调用，
    public void addCoreStatusListener(CoreStatusListener listener) {
        coreStatusListeners.add(listener);
    }

    public String getToken() {
        return coreManager.getSelfStatus().accessToken;
    }

    public User getUser() {
        return coreManager.getSelf();
    }

    public AppConfig getAppConfig() {
        return coreManager.getConfig();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCore();
    }

    private void initCore() {
        Log.d(TAG, "initCore() called");
        if (coreManager == null) {
            coreManager = new CoreManager(this, this);
        }
        if (coreStatusListeners == null) {
            coreStatusListeners = new ArrayList<>();
        }
        coreManager.init(loginRequired, configRequired);
    }

    @Override
    public void onCoreReady() {
        Log.d(TAG, "onCoreReady() called");
        for (CoreStatusListener listener : coreStatusListeners) {
            listener.onCoreReady();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
