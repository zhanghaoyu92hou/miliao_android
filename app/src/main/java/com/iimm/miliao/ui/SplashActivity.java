package com.iimm.miliao.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.iimm.miliao.AppConfig;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.BuildConfig;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.adapter.MessageLogin;
import com.iimm.miliao.bean.ConfigBean;
import com.iimm.miliao.bean.EventBusMsg;
import com.iimm.miliao.bean.MediaStartupBean;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.ui.account.LoginActivity;
import com.iimm.miliao.ui.account.LoginHistoryActivity;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.notification.NotificationProxyActivity;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.EventBusHelper;
import com.iimm.miliao.util.LogUtils;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.VersionUtil;
import com.iimm.miliao.util.permission.AndPermissionUtils;
import com.iimm.miliao.util.permission.OnPermissionClickListener;
import com.iimm.miliao.util.permission.PermissionDialog;
import com.iimm.miliao.view.TipDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 启动页
 */
public class SplashActivity extends BaseActivity implements OnPermissionClickListener {
    private static String TAG = "SplashActivity";
    private ImageView mWelcomeIv;
    // 配置是否成功
    private boolean mConfigReady = false;

    public SplashActivity() {
        // 这个页面不需要已经获取config, 也不需要已经登录，
        noConfigRequired();
        noLoginRequired();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceUtils.putBoolean(this, "isBoot", false);
        PreferenceUtils.putString(MyApplication.getInstance(), "send_200_packageId", "");
        Log.d("zxzx", "onCreate: " + SplashActivity.class.getSimpleName());
        Intent intent = getIntent();
        LogUtils.log(TAG, intent);
        externalTuningUp();
        if (NotificationProxyActivity.processIntent(intent)) {
            // 如果是通知点击进来的，带上参数转发给NotificationProxyActivity处理，
            intent.setClass(this, NotificationProxyActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        // 如果不是任务栈第一个页面，就直接结束，显示上一个页面，
        // 主要是部分设备上Jitsi_pre页面退后台再回来会打开这个启动页flg=0x10200000，此时应该结束启动页，回到Jitsi_pre,
        if (!isTaskRoot()) {
            finish();
            return;
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_splash);
        mWelcomeIv = findViewById(R.id.welcome_iv);
        //先注释
      //  Glide.with(SplashActivity.this).load(PreferenceUtils.getString(SplashActivity.this, "imageUrl")).dontAnimate().into(mWelcomeIv);

        // 初始化配置
        initConfig();

        EventBusHelper.register(this);

    }

    public void externalTuningUp() {
        // 获取uri参数
        Intent intent = getIntent();
        String scheme = intent.getScheme();
        Uri uri = intent.getData();
        if (uri != null) {
            Log.e("splash", uri.toString());
            AppConfig.EXTERNAL_TUNING_UP = uri.toString();
            EventBusMsg eventBusMsg = new EventBusMsg();
            eventBusMsg.setMessageType(Constants.EVENTBUS_GROUP_QRCODE);
            EventBus.getDefault().post(eventBusMsg);
        } else {
            AppConfig.EXTERNAL_TUNING_UP = "";
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageLogin message) {
        finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if(TextUtils.isEmpty(coreManager.getConfig().androidDisable) || !blockVersion(coreManager.getConfig().androidDisable, coreManager.getConfig().androidAppUrl) ){
            // 请求权限过程中离开了回来就再请求吧，
            AndPermissionUtils.forcedAcquisition(this, this);
        }
    }

    /*
    public static String httpGet(String path){
        try {
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            //获得结果码
            int responseCode = connection.getResponseCode();
            if(responseCode ==200){
                //请求成功 获得返回的流
                InputStream is = connection.getInputStream();
                //读取输入流
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                return result.toString();
            }else {
                //请求失败
                return null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    */

    /**
     * 配置参数初始化
     */
    private void initConfig() {
        getConfig();
    }

    private void getConfig() {
        String mConfigApi = AppConfig.readConfigUrl(mContext);

        Map<String, String> params = new HashMap<>();
        Reporter.putUserData("configUrl", mConfigApi);

        HttpUtils.get().url(mConfigApi)
                .params(params)
                .build()
                .execute(new BaseCallback<ConfigBean>(ConfigBean.class) {
                    @Override
                    public void onResponse(ObjectResult<ConfigBean> result) {
                        ConfigBean configBean;
                        if (result == null || result.getData() == null || result.getResultCode() != Result.CODE_SUCCESS) {
                            com.iimm.miliao.util.log.LogUtils.e(TAG, "获取网络配置失败，使用已经保存了的配置");
//                            if (BuildConfig.DEBUG) {
//                                ToastUtil.showToast(SplashActivity.this, R.string.tip_get_config_failed);
//                            }
                            ToastUtil.showToast(SplashActivity.this, R.string.tip_get_config_failed);
                            return;
                            // 获取网络配置失败，使用已经保存了的配置，
                            //configBean = coreManager.readConfigBean();
                        } else {
                            Log.e(TAG, "获取网络配置成功，使用服务端返回的配置并更新本地配置");
                            configBean = result.getData();
                            if (!TextUtils.isEmpty(configBean.getAddress())) {
                                PreferenceUtils.putString(SplashActivity.this, AppConstant.EXTRA_CLUSTER_AREA, configBean.getAddress());
                            }
                            if (Constants.SUPPORT_MANUAL_NODE) {
                                if (configBean.getIsNodesStatus() == Constants.LOGIN_NODE_SUPPORT) {
                                    if (configBean.getNodesInfoList() != null && configBean.getNodesInfoList().size() > 0 && TextUtils.isEmpty(PreferenceUtils.getString(SplashActivity.this, AppConstant.VALID_XMPP_HOST, ""))) {
                                        String xmppHost = configBean.getNodesInfoList().get(0).getNodeIp();
                                        if (!TextUtils.isEmpty(xmppHost)) {
                                            MyApplication.validXmppHost = xmppHost;
                                            PreferenceUtils.putString(SplashActivity.this, AppConstant.VALID_XMPP_HOST, xmppHost);
                                        }
                                        if (!TextUtils.isEmpty(configBean.getNodesInfoList().get(0).getNodePort())) {
                                            try {
                                                int xmppPort = Integer.parseInt(configBean.getNodesInfoList().get(0).getNodePort());
                                                MyApplication.validXmppPort = xmppPort;
                                                PreferenceUtils.putInt(SplashActivity.this, AppConstant.VALID_XMPP_PORT, xmppPort);
                                            } catch (Exception e) {
                                                PreferenceUtils.putInt(SplashActivity.this, AppConstant.VALID_XMPP_PORT, AppConfig.mXMPPPort);
                                            }
                                            if (!TextUtils.isEmpty(configBean.getNodesInfoList().get(0).getNodeName())) {
                                                PreferenceUtils.putString(SplashActivity.this, AppConstant.VALID_XMPP_NAME, configBean.getNodesInfoList().get(0).getNodeName());
                                            }
                                        }
                                    }
                                }
                            }
                            coreManager.saveConfigBean(configBean);
                            MyApplication.IS_OPEN_CLUSTER = configBean.getIsOpenCluster() == 1;
                        }
                        setConfig(configBean);
                        //先注释
                      //  getImage();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Log.e("zq", "获取网络配置失败，使用已经保存了的配置");
                        ToastUtil.showToast(SplashActivity.this, R.string.tip_get_config_failed);
                        return;
                        // ToastUtil.showToast(SplashActivity.this, R.string.tip_get_config_failed);
                        // 获取网络配置失败，使用已经保存了的配置，
                        //ConfigBean configBean = coreManager.readConfigBean();
                        //setConfig(configBean);
                    }
                });
    }

    private void getImage() {
        Map<String, String> params = new HashMap<>();
        HttpUtils.get().url(coreManager.getConfig().MEDIA_STARTUP)
                .params(params)
                .build()
                .execute(new ListCallback<MediaStartupBean>(MediaStartupBean.class) {
                    @Override
                    public void onResponse(ArrayResult<MediaStartupBean> result) {
                        if (result.getResultCode() == 1 && result.getData() != null && result.getData().size() > 0) {
                            if (isDestroyed()) {  //界面关闭后不进行 数据回调处理
                                return;
                            }
                            if (!TextUtils.equals(result.getData().get(0).getAddr(), PreferenceUtils.getString(SplashActivity.this, "imageHttpUrl"))) {
                                Glide.with(SplashActivity.this)
                                        .load(result.getData().get(0).getAddr())
                                        .asBitmap()
                                        .listener(new RequestListener<String, Bitmap>() {
                                            @Override
                                            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                                String imageUrl = saveBitmap(resource);
                                                if (!TextUtils.isEmpty(imageUrl)) {
                                                    PreferenceUtils.putString(SplashActivity.this, "imageUrl", imageUrl);
                                                    PreferenceUtils.putString(SplashActivity.this, "imageHttpUrl", result.getData().get(0).getAddr());
                                                }
                                                return false;
                                            }
                                        })
                                        .dontAnimate()
                                        .into(mWelcomeIv);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Log.e(TAG, "获取图片失败");
                    }
                });
    }

    private void setConfig(ConfigBean configBean) {
        if (configBean == null) {
            if (BuildConfig.DEBUG) {
                ToastUtil.showToast(this, R.string.tip_get_config_failed);
            }

            // 如果没有保存配置，也就是第一次使用，就连不上服务器，使用默认配置
            configBean = CoreManager.getDefaultConfig(this);
            if (configBean == null) {
                // 不可到达，本地assets一定要提供默认config,
                DialogHelper.tip(this, getString(R.string.tip_get_config_failed));
                return;
            }
            coreManager.saveConfigBean(configBean);
        }
        LogUtils.log(TAG, configBean);
        // 配置完毕
        mConfigReady = true;
        // 如果没有androidDisable字段就不判断，
        // 当前版本没被禁用才继续打开，
        if (TextUtils.isEmpty(configBean.getAndroidDisable()) || !blockVersion(configBean.getAndroidDisable(), configBean.getAndroidAppUrl())) {
            // 进入主界面
            ready();
        }
    }

    /**
     * 如果当前版本被禁用，就自杀，
     *
     * @param disabledVersion 禁用该版本以下的版本，
     * @param appUrl          版本被禁用时打开的地址，
     * @return 返回是否被禁用，
     */
    private boolean blockVersion(String disabledVersion, String appUrl) {
        String currentVersion = BuildConfig.VERSION_NAME;
        if (VersionUtil.compare(currentVersion, disabledVersion) > 0) {
            // 当前版本大于被禁用版本，
            return false;
        } else {
            // 通知一下，
            DialogHelper.tip(this, getString(R.string.tip_version_disabled));
            new TipDialog(this).setOnDismissListener(dialog -> {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(appUrl));
                    startActivity(i);
                } catch (Exception e) {
                    // 弹出浏览器失败的话无视，
                    // 比如没有浏览器的情况，
                    // 比如appUrl不合法的情况，
                }
                // 自杀，
                finish();
                MyApplication.getInstance().destory();
            });
            return true;
        }
    }

    private void ready() {
        if (!mConfigReady) {// 配置失败
            return;
        }
        AndPermissionUtils.forcedAcquisition(this, this);
    }

    @SuppressLint("NewApi")
    private void jump() {
        if (isDestroyed()) {
            return;
        }
        int userStatus = LoginHelper.prepareUser(mContext, coreManager);
        Intent intent = new Intent();
        //先注释
       switch (userStatus) {
            case LoginHelper.STATUS_USER_FULL:
            case LoginHelper.STATUS_USER_NO_UPDATE:
            case LoginHelper.STATUS_USER_TOKEN_OVERDUE:
                boolean login = PreferenceUtils.getBoolean(this, Constants.LOGIN_CONFLICT, false);
                if (login) {// 登录冲突，退出app再次进入，跳转至历史登录界面
                    intent.setClass(mContext, LoginHistoryActivity.class);
                } else {
                    intent.setClass(mContext, MainActivity.class);

                }
                break;
            case LoginHelper.STATUS_USER_SIMPLE_TELPHONE:
                intent.setClass(mContext, LoginHistoryActivity.class);
                break;
            case LoginHelper.STATUS_NO_USER:
            default:
                stay();
                return;// must return
        }

        startActivity(intent);
        finish();
    }

    // 第一次进入，显示登录、注册按钮
    private void stay() {
        // 因为启动页有时会替换，无法做到按钮与图片的完美适配，干脆直接进入到登录页面
        CountDownTimer countDownTimer = new CountDownTimer(3500, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "倒计时" + (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                startActivity(new Intent(mContext, LoginActivity.class));
                finish();
            }
        }.start();
    }

    @Override
    public void onSuccess() {
        jump();
    }

    @Override
    public void onFailure(List<String> data) {
        PermissionDialog.show(this, data);
    }

    public static String saveBitmap(Bitmap bitmap) {
        File imageDir = new File(Environment.getExternalStorageDirectory(), "image");
        if (!imageDir.exists()) {
            imageDir.mkdir();
        }

        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(imageDir, fileName);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
