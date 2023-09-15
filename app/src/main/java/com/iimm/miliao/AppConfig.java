package com.iimm.miliao;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.iimm.miliao.bean.ConfigBean;
import com.iimm.miliao.bean.NodeInfo;
import com.iimm.miliao.util.PreferenceUtils;

import java.util.List;
import java.util.Objects;

import static com.iimm.miliao.util.Constants.HUIWEI_OBS;
import static com.iimm.miliao.util.Constants.TENTENT_COS;

public class AppConfig {
    public static final String TAG = "AppConfig";
    public static final boolean DEBUG = true;
    public static String EXTERNAL_TUNING_UP = "";

    /* 应用程序包名 */
    // 这个用于过滤广播，必须不同应用使用不同字符串，否则广播串线，
    public static final String sPackageName = BuildConfig.APPLICATION_ID;
    public static final String apiKey = "";   //加密Key %In9AXC0#Za8kd&U
    public static final String BroadcastReceiverClass = MyBroadcastReceiver.class.getName();

    public static final String BROADCASTTEST_ACTION = BuildConfig.APPLICATION_ID + ".action.broadcasttest.startActivity";
    /* 分页的Size */
    public static final int PAGE_SIZE = 50;


    // 改服务器时要同时改默认配置assets/default_config和AppConfig.apiKey,
    /* 请求配置的接口 */
    public static String host = BuildConfig.CONFIG_HOST;
    public static String port = BuildConfig.CONFIG_PORT;
    public static String CONFIG_URL = host + ":" + port + "/config";// 正式的服务器
    /* 基本地址 */
    public String second_channel_url;//第二通道url
    public String apiUrl;// Api的服务器地址
    public String uploadUrl;// 上传的服务器地址
    public String downloadAvatarUrl;// 头像下载地址
    public String downloadUrl;// 头像以外的东西的下载地址
    public String XMPPHost;// Xmpp服务器地址
    public static int mXMPPPort = 5222;
    public String XMPPDomain;
    public int xmppPingTime;// 每隔xmppPingTime秒ping一次服务器
    public int XMPPTimeOut; // Xmpp超时时长(服务器针对客户端的超时时长)
    public String JitsiServer="https://meet.ttechworld.com/";// jitsi前缀地址
    /* Api地址--》衍生地址 */
    /* 注册登录 */
    public String USER_LOGIN;// 登陆
    public String USER_THIRD_LOGIN_NEW;//第三方登陆（这个是新接口，可以直接登陆不需要绑定手机号或用户名）
    public String OTHER_SET_INVITE_CODE;//第三方登录绑定邀请码
    public String BAND_THIRD_PARTY_NEW;//第三方绑定新版
    public String USER_REGISTER;// 注册
    public String USER_PASSWORD_UPDATE;// 用户修改密码
    public String USER_PASSWORD_RESET;// 用户重置密码
    public String QUESTION_CHECK;//用户名时候，校验密保问题
    public String USER_LOGIN_AUTO;// 检测Token是否过期 0未更换 1更换过
    public String USER_GETCODE_IMAGE; // 获取图形验证码
    public String SEND_AUTH_CODE;// 获取手机验证码
    public String VERIFY_TELEPHONE;// 验证手机号有没有被注册
    /* 用户 */
    public String USER_UPDATE; // 用户资料修改
    public String USER_GET_URL;// 获取用户资料，更新本地数据的接口
    public String USER_PHOTO_LIST;// 获取相册列表
    public String USER_QUERY;// 查询用户列表
    public String USER_NEAR; // 查询搜索列表
    public String PUBLIC_SEARCH; // 公众号搜索列表
    public String USER_SET_PRIVACY_SETTING;// 设置用户隐私设置
    public String USER_GET_PRIVACY_SETTING;// 获取用户隐私设置

    public String USER_GET_BAND_ACCOUNT;// 获取用户绑定账号
    public String USER_UN_BAND_ACCOUNT;// 修改用户绑定账号
    public String OTHER_BIND_Phone_Pass_Word;//绑定手机号
    public String AUTHOR_CHECK; // 授权登录弹窗url

    public String USER_GET_PUBLIC_MENU; // 获取公众号菜单
    public String USER_DEL_CHATMESSAGE; // 删除聊天记录
    public String USER_ADD_COURSE; // 添加课程
    public String USER_QUERY_COURSE; // 查询课程
    public String USER_EDIT_COURSE;  // 修改课程
    public String USER_DEL_COURSE;   // 删除课程
    public String USER_COURSE_DATAILS; // 课程详情
    /* 附近 */
    public String NEARBY_USER;// 获取附近的用户
    /* 商务圈 */
    public String APP_DISCOVER_LIST;//获取发现页
    public String USER_CIRCLE_MESSAGE;// 获取某个人的商务圈消息
    public String DOWNLOAD_CIRCLE_MESSAGE;// 下载商务圈消息
    /* 好友 */
    public String FRIENDS_ATTENTION_LIST;// 获取关注列表
    public String FRIENDS_BLACK_LIST;// 获取黑名单列表

    public String FRIENDS_ATTENTION_ADD;// 加关注 || 直接成为好友
    public String ADD_FRIENDS;// 同意成为好友
    public String FRIENDS_BLACKLIST_ADD;// 拉黑
    public String FRIENDS_BLACKLIST_DELETE;// 取消拉黑
    public String FRIENDS_ATTENTION_DELETE;// 取消关注
    public String FRIENDS_DELETE;// 删除好友

    public String FRIENDS_REMARK;// 备注好友
    public String FRIENDS_UPDATE; // 消息保存天数
    public String FRIENDS_NOPULL_MSG; // 消息免打扰
    /* 群聊 */
    public String ROOM_ADD;// 创建群组
    public String ROOM_JOIN;// 加入群组
    public String ROOM_MEMBER_UPDATE;// 添加成员
    public String ROOM_DELETE;// 删除群组
    public String ROOM_MEMBER_DELETE;// 删除成员 || 退出群组
    public String ROOM_MEMBERS_DELETE;//批量删除
    public String ROOM_TRANSFER;// 转让群组

    public String ROOM_GET;// 获取群组
    public String ROOM_LIST;// 获取群组列表
    public String ROOM_MEMBER_GET;// 获取成员
    public String ROOM_MEMBER_LIST;// 获取成员列表
    public String ROOM_LIST_HIS;// 获取某个用户已经加入的房间列表
    public String ROOM_GET_ROOM;// 获取自己的成员信息以及群属性
    public String ROOM_UPDATE;// 设置群组
    public String ROOM_MANAGER;// 指定管理员
    public String ROOM_UPDATE_ROLE;// 指定隐身人，监控人，
    public String ROOM_DELETE_NOTICE;// 删除群公告
    public String ROOM_LOCATION_QUERY;// 面对面建群查询
    public String ROOM_LOCATION_JOIN;// 面对面建群加入
    public String ROOM_LOCATION_EXIT;// 面对面建群退出
    public static String ROOM_GROUP_SIGN;// 群主签到
    public static String ROOM_GROUP_SIGN_DETAIL;//签到详情
    public static String ROOM_GROUP_SIGN_REWARD;//签到兑换奖励
    public static String ROOM_GROUP_SIGN_RECORD;//签到记录
    public static String ROOM_GROUP_INFO_BY_MONTH;//签到日历
    public static String ROOM_GROUP_QRCODE_SHARE;//签到二维码

    public String ROOM_DISTURB;// 消息免打扰

    public String OPEN_GET_HELPER_LIST;// 获取所有群助手列表
    public String ROOM_ADD_GROUP_HELPER;// 添加群助手
    public String ROOM_DELETE_GROUP_HELPER;// 移除群助手
    public String ROOM_QUERY_GROUP_HELPER;// 查询群助手
    public String ROOM_ADD_AUTO_RESPONSE;//添加自动回复
    public String ROOM_UPDATE_AUTO_RESPONSE;//修改自动回复
    public String ROOM_DELETE_AUTO_RESPONSE;//删除自动回复
    /* 商务圈 */
    public String MSG_WITH_CONDITION;//搜索某个人朋友圈
    public String MSG_ADD_URL;// 发布一条公共消息的接口
    public String MSG_LIST;// 获取公共消息的接口
    public String MSG_USER_LIST;// 获取某个用户的最新公共消息
    public String MSG_GETS;// 根据IDS批量获取公共消息的接口(我的商务圈使用)
    public String MSG_GET;// 根据ID获取公共消息
    public String CIRCLE_MSG_DELETE;// 删除一条商务圈消息
    public String MSG_PRAISE_ADD;// 赞
    public String MSG_PRAISE_DELETE;// 取消赞
    public String MSG_PRAISE_LIST;// 点赞分页加载的接口，
    public String MSG_COLLECT_DELETE;//  取消朋友圈收藏，
    public String CIRCLE_MSG_LATEST;// 获取人才榜最新消息
    public String CIRCLE_MSG_HOT;// 最热人才榜
    public String MSG_COMMENT_ADD;// 增加一条评论
    public String MSG_COMMENT_DELETE;// 删除一条评论
    public String MSG_COMMENT_LIST;// 获取评论列表
    /* 上传 的服务器地址--》衍生地址 */
    public String UPLOAD_URL;// 上传图片接口
    public String AVATAR_UPLOAD_URL;// 上传头像接口
    public String ROOM_UPDATE_PICTURE;// 上传群头像接口
    public String UPLOAD_MUC_FILE_ADD;// 上传群文件接口
    public String UPLOAD_MUC_FILE_FIND_ALL;// 查询所有文件
    public String UPLOAD_MUC_FILE_FIND;// 查询单个文件
    public String UPLOAD_MUC_FILE_DEL;//  删除单个群文件
    /* 头像下载地址--》衍生地址 */
    public String AVATAR_ORIGINAL_PREFIX;// 头像原图前缀地址
    public String AVATAR_THUMB_PREFIX;   // 头像缩略图前缀地址
    /* 登出地址 */
    public String USER_LOGOUT;
    /* 保存最后一次在线时间地址 */
    public String USER_OUTTIME;
    /* 红包相关的URL变量*/
    public String REDPACKET_CREATE;//发云红包
    public String REDPACKET_CREATE_INQUIRE;//查询云红包
    public String REDPACKET_GRAD;//抢云红包
    public String CLOUD_PROTOCOL;//云红包协议 protocol
    public String MICRO_TRANSFER_SEND_TRANSFER;//微转账 预下单
    public String MICRO_RECHARGE_GET;//微转账 查询
    public String MICRO_RECHARGE_ACCEPT;//接受微转账

    public String RECHARGE_ADD; // 余额充值
    public String RECHARGE_GET; // 余额查询
    public String REDPACKET_SEND; // 发送红包
    public String RENDPACKET_GET; // 获取红包详情
    public String RENDPACKET_REPLY; // 回复红包
    public String REDPACKET_OPEN; // 打开红包
    public String SEND_REDPACKET_LIST_GET; // 获取发出的红包
    public String RECIVE_REDPACKET_LIST_GET; // 获取收到的红包
    public String TRANSFER_SEND_TRANSFER; // 转账
    public String TRANSFER_GET_TRANSFERINFO; // 获取转账信息
    public String TRANSFER_RECEIVE_TRANSFER; // 接受转账
    public String PAY_CODE_PAYMENT; // 向展示付款码的用户收钱
    public String PAY_CODE_RECEIPT;// 向展示收款码的用户付钱
    public String TRANSACTION_RECORD;
    public String CONSUMERECORD_GET; // 获取消费记录列表
    public String VX_RECHARGE;    // 微信/支付宝 充值  新增h5充值
    public String VX_UPLOAD_CODE; // 微信 上传
    public String VX_GET_OPEN_ID; // 微信 上传
    public String VX_TRANSFER_PAY;// 微信 提现
    public String ALIPAY_AUTH;// 支付宝 授权
    public String ALIPAY_BIND;// 支付宝 绑定
    public String ALIPAY_TRANSFER;// 支付宝 提现

    public String PAY_GET_ORDER_INFO;// 获取订单信息
    public String PAY_PASSWORD_PAYMENT;// 输入密码后支付接口
    /* 抖音视频获取接口 */
    public String GET_TRILL_LIST;// 获取视频列表
    public String GET_MUSIC_LIST;// 获取音乐列表 http://47.91.232.3:8092/music/list?access_token=20bc3cbda93241a8903a999a5ad71625&pageIndex=0&pageSize=20&keyword=94
    /* 直播 */
    public String GET_LIVE_ROOM_LIST;// 获取直播间列表
    public String CREATE_LIVE_ROOM;// 创建直播间
    public String JOIN_LIVE_ROOM;// 加入直播间
    public String EXIT_LIVE_ROOM;// 退出直播间
    public String DELETE_LIVE_ROOM;// 删除直播间
    public String LIVE_ROOM_DETAIL;// 直播间详情
    public String LIVE_ROOM_MEMBER_LIST;// 直播间成员列表
    public String LIVE_ROOM_DANMU;// 发送弹幕
    public String GET_LIVE_GIFT_LIST;// 获取礼物列表
    public String LIVE_ROOM_GIFT;// 发送礼物
    public String LIVE_ROOM_PRAISE;// 发送爱心
    public String LIVE_ROOM_GET_IDENTITY;// 获取身份信息
    public String LIVE_ROOM_UPDATE;// 修改直播间
    public String LIVE_ROOM_SET_MANAGER;// 设置管理员
    public String LIVE_ROOM_SHUT_UP;// 禁言/取消禁言
    public String LIVE_ROOM_KICK;// 踢人
    public String LIVE_ROOM_STATE;// 正在直播 || 未开启直播
    public String LIVE_GET_LIVEROOM;// 获取自己的直播间
    public String EMPTY_SERVER_MESSAGE;// 清空服务端数据
    /* 组织架构 */
    public String AUTOMATIC_SEARCH_COMPANY;//自动查询公司
    public String CREATE_COMPANY; // 创建公司
    public String SET_COMPANY_MANAGER; // 指定管理者
    public String MODIFY_COMPANY_NAME; // 修改公司名称
    public String CHANGE_COMPANY_NOTIFICATION; // 更改公司公告
    public String SEARCH_COMPANY; // 查找公司
    public String DELETE_COMPANY; // 删除公司
    public String CREATE_DEPARTMENT; // 创建部门
    public String MODIFY_DEPARTMENT_NAME; // 修改部门名称
    public String DELETE_DEPARTMENT; // 删除部门
    public String ADD_EMPLOYEE; // 添加员工
    public String DELETE_EMPLOYEE; // 删除员工
    public String MODIFY_EMPLOYEE_DEPARTMENT; // 更改员工部门
    public String COMPANY_LIST; // 公司列表
    public String DEPARTMENT_LIST; // 部门列表
    public String EMPLOYEE_LIST; // 员工列表
    public String GET_COMPANY_DETAIL; // 获取公司详情
    public String GET_EMPLOYEE_DETAIL; // 获取员工详情
    public String GET_DEPARTMENT_DETAIL; // 获取部门详情
    public String GET_EMPLOYEE_NUMBER_OF_COMPANY; // 获取公司员工人数
    public String EXIT_COMPANY;// 退出公司
    public String CHANGE_EMPLOYEE_IDENTITY;// 修改身份公司
    /* 标签 */
    public String FRIENDGROUP_LIST;// 获取标签列表
    public String FRIENDGROUP_ADD;// 添加标签
    public String FRIENDGROUP_DELETE;// 删除标签
    public String FRIENDGROUP_UPDATE;// 修改标签名
    public String FRIENDGROUP_UPDATEGROUPUSERLIST;// 标签下的好友增、删
    public String FRIENDGROUP_UPDATEFRIEND;
    /* 消息漫游 */
    public String GET_LAST_CHAT_LIST; // 获取最近一条的聊天记录列表
    public String GET_CHAT_MSG; // 获取单聊漫游的消息
    public String GET_CHAT_MSG_MUC; // 获取群聊漫游的消息
    /* 收藏 */
    public String Collection_ADD; // 添加收藏
    public String Collection_REMOVE;// 删除收藏
    public String Collection_LIST;// 表情列表
    public String Collection_LIST_OTHER;// 收藏列表
    public String USER_REPORT;// 举报用户 || 群组 || 网站
    public String UPLOAD_COPY_FILE;// 服务端将文件拷贝一份，更换另外一个url地址返回
    public String ADDRESSBOOK_UPLOAD;// 上传本地联系人
    public String ADDRESSBOOK_GETALL;// 查询通讯录好友
    public String ADDENTION_BATCH_ADD;// 联系人内加好友 不需要验证
    // 支付密码，
    public String CHECK_PAY_PASSWORD;  // 检查支付密码，
    public String UPDATE_PAY_PASSWORD;// 修改支付密码，
    public String QUESTION_LIST;//获取密保问题
    public String QUESTION_SET;//设置密保问题
    public String GET_COS_KEY;//获取腾讯Cos 临时授权信息
    public String LOGIN_PUBLIC_NUMBER;//扫码登录公众号
    public String LOGIN_PC;//pc端扫码登录
    public String LOGIN_PUBLIC_OPEN_NUMBER;//开放平台扫码登录
    public String POINTS_ASSISTANT;//积分小助手

    public String GET_BANK_LIST;  //银行卡列表
    public String SUBMIT_BANK_INFO_PAY;  //提交支付信息

    public String GET_MY_BANK_CARD_LIST;//我的支付银行卡列表

    public String ADD_BANK_CARD;//添加银行卡
    public String DEL_MY_BANK_CARD;//删除银行卡

    public String MEDIA_STARTUP;//获取启动页图片
    //开放平台
    public String AUTHORIZATION;//验证账号是否开通
    public String CODE_OAUTH;//根据Code获取资料
    // 集群 获取 meetUrl
    public String OPEN_MEET;
    // 以下是所有推送上传推送ID的接口，
    // 最后调用的是哪个，服务器端就是用哪个推送，
    // 小米推送接口
    public String configMi;
    // 华为推送接口
    public String configHw;
    // 极光推送接口
    public String configJg;
    // vivo推送接口
    public String configVi;
    // oppo推送接口
    public String configOp;
    // firebase推送接口
    public String configFcm;
    // 魅族推送接口
    public String configMz;
    // 登录分享sdk验证接口
    public String SDK_OPEN_AUTH_INTERFACE;
    // 获取服务器时间接口，用于校准时间，
    public String GET_CURRENT_TIME;
    public String URL_CHECK;
    public String GET_USER_SIGN_INFO_BY_MONTH;//获取某月签到信息
    public String SIGN_IN_NOW;//立即签到
    public String GET_USER_SIGN_INFO;//获取签到信息
    public String TI_XIAN;//提现
    // 是否开放注册，默认开放，服务器没返回这条配置也是默认开放，
    public boolean isOpenRegister;
    // 注册时是否需要验证码，
    public boolean isOpenSMSCode;
    public boolean isQestionOpen;//是否开启密保问题
    public String headBackgroundImg;
    public String GET_NEW_VERSION;//获取新版本号
    /* 表情*/
    public String LOAD_BQ_SHOP_LIST;
    public String ADD_BQ;
    public String LOAD_MY_BQ;
    public String REMOVE_MY_BQ;


    /*OBS  COS*/
    public int IS_OPEN_OBS_STATUS;//是否开启了obs 服务，1开启华 0 未开启
    public String ACCESS_KEY_ID; //ak  obs 用户 唯一 表示  ，已通过 RSA 加密
    public String ACCESS_SECRET_KEY;// sk   obs 密钥  配合 ak 使用  已通过 RSA 加密
    public String OBS_BUCKET_NAME;// 桶 名称  服务器提供，所有上传文件存入 此桶
    public String OBS_END_POINT;//访问点
    public String OBS_LOCATION;// 访问区域 服务器已为桶配置 无需配置
    public int OBS_OS_TYPE; //当前obs 系统
    public String OBS_OS_NAME; // 当前obs 系统 name 名称
    public String OBS_OS_APP_ID;// 只有腾讯云COS 才会用到


    public String Pay_Forgot_PassWord;//重置支付密码
    public String LOG_REPORT;//上报异常日志
    public String CLEAR_GROUP_CHAT; //清空群聊天记录
    public String DELETE_MSG_LIST_ONE; //删除会话列表中的 某一个  messageFragment
    public String TWO_WAY_WITHDRAWAL; // 双向撤回

    public String androidDisable;//禁止版本号
    /**
     * 注册邀请码   registerInviteCode
     * 0:关闭
     * 1:开启一对一邀请（一码一用，且必填）
     * <p>
     * 2:开启一对多邀请（一码多用，选填项），该模式下客户端需要把用户自己的邀请码显示出来
     */
    // 注册时是否需要邀请码，
    public int registerInviteCode;
    // 是否允许昵称搜索
    public boolean cannotSearchByNickName;
    // 是否使用用户名注册，
    public int registerUsername;
    // 普通用户是否能搜索好友, true表示不可以，
    public boolean ordinaryUserCannotSearchFriend;
    // 普通用户是否能建群, true表示不可以，
    public boolean ordinaryUserCannotCreateGroup;
    // 关于页面的信息，
    public String companyName;
    public String copyright;
    public int isUserSignRedPacket; //是否需要签到红包(因为签到红包改到发现页面了，所以这里暂时不使用了)
    public int isWithdrawToAdmin; //是否提现到后台
    public double minWithdrawToAdmin;//最小 提现金额  单位 元
    // 是否隐藏好友搜索功能, true表示隐藏，
    public boolean isHideSearchFriend;
    // 是否禁用红包相关功能 true表示禁用，
    public boolean displayRedPacket;
    // 是否禁用位置相关功能，true表示禁用，
    public boolean disableLocationServer;
    // 是否启用谷歌推送，true表示启用，
    public boolean enableGoogleFcm;
    // 热门应用  lifeCircle  生活圈，  videoMeeting 视频会议，  liveVideo 视频直播，  shortVideo 短视频， peopleNearby 附近的人
    public ConfigBean.PopularApp popularAPP;
    // 二维码地址，
    public String website;
    // 是否使用新ui界面，主要影响发现页面，旧UI直接是生活圈，
    public boolean newUi = true;
    // 是否启用第三方登录，比如微信登录，定制没配置微信appId的话就不用显示相关按钮了，
    //public boolean thirdLogin = true;
    public String androidAppUrl;// AndroidApp下载地址
    public String androidVersion;// 版本号
    public int isNodesStatus;// 是否支持多节点登录。0 不支持；1 支持
    public List<NodeInfo> nodesInfoList;
    public ConfigBean.TabBarConfigListBean getTabBarConfigList;
    public static String wechatAppId;//微信ID
    public int wechatPayStatus;//是否支持微信充值
    public int wechatWithdrawStatus;//是否支持微信提现
    public int aliPayStatus;//是否支持支付宝充值
    public int aliWithdrawStatus;//是否支持支付宝提现
    public int qqLoginStatus;//是否支持QQ登录
    public int wechatLoginStatus;//是否支持微信登录
    public String qqLoginAppId;//QQID

    public int IS_OPEN_TWO_BAR_CODE;
    public int IS_OPEN_TEL_NUM;
    // 做 兼容 用了两个值
    public int bankPayStatus; //是否支持银行卡充值
    public int bankPayStatus2; //是否支持银行卡充值
    public String transferRate;//费率

    public String WITHDRAW_METHOD_SET;//新增支付宝或银行卡
    public String DELETE_BLANCE;//删除银行卡、支付宝
    public String LIST_BLANCE;//银行卡、支付宝列表
    public String GET_WITHDRAW_WAY;//动态提现后台
    public int hmPayStatus;//黑马支付充值状态 1开启 2关闭
    public int hmWithdrawStatus;//黑马支付提现状态 1开启 2关闭

    public int isWeiBaoStatus;//是否开通云钱包
    public String weiBaoMaxRedPacketAmount;//微红包最大金额
    public String weiBaoMaxTransferAmount;//微钱包最大提现金额
    public String weiBaoMinTransferAmount;//微钱包最小提现金额
    public String weiBaoTransferRate;//微提现费率
    public String maxSendRedPagesAmount;//群红包最大金额
    public int isDelAfterReading;//是否开启阅后即焚 0开启，1关闭
    public int isAudioStatus;//是否开启音视频 0 关闭 1开启
    public int isEnableCusServer;//是否开启联系客服  0关闭  1开启
    public String cusServerUrl;//联系客服url


    public static boolean isChat() {
        return Objects.equals(BuildConfig.APPLICATION_ID, "com.im.liao"); // 安卓app包名
    }

    public static String readConfigUrl(Context ctx) {
        String appUrl = PreferenceUtils.getString(ctx, "APP_SERVICE_CONFIG");
        Log.i(TAG, "readConfigUrl: appUrl:" + appUrl);
        if (TextUtils.isEmpty(appUrl)) {// 未手动输入过服务器配置，使用默认地址
            appUrl = AppConfig.CONFIG_URL;
            // 保存默认地址，下次使用，
            // 避免其他服务器新包覆盖时自动登录出现错乱，
            saveConfigUrl(ctx, appUrl);
        }
        Log.i(TAG, "readConfigUrl: appUrl:" + appUrl.replaceAll(" ", ""));
        return appUrl.replaceAll(" ", "");// 手动输入可能会有一些空格，替换掉
    }

    public static void saveConfigUrl(Context ctx, String str) {
        String url;
        if (str.endsWith("/config"))
            url = str;
        else {
            url = str + "/config";
        }
        PreferenceUtils.putString(ctx, "APP_SERVICE_CONFIG", url);
    }

    /**
     * 会改变的配置
     **/
    public static AppConfig initConfig(ConfigBean configBean) {
        AppConfig config = new AppConfig();
        config.androidDisable = configBean.getAndroidDisable();
        /* 1、Api 的服务器地址 */
        config.apiUrl = configBean.getApiUrl();

        /* 2、上传的服务器地址 */
        config.uploadUrl = configBean.getUploadUrl();

        /* 3、头像下载地址 */
        config.downloadAvatarUrl = configBean.getDownloadAvatarUrl();
        config.downloadUrl = configBean.getDownloadUrl();

        // 是否请求回执 0 不请求 1 请求
        int isOpenReceipt = configBean.getIsOpenReceipt();
        MyApplication.IS_OPEN_RECEIPT = isOpenReceipt == 1 ? true : false;

        // 是否开放注册，
        String isOpenRegisterStr = configBean.getIsOpenRegister();
        boolean isOpenRegister = true;
        // 为0表示不开放注册，为1或者不存在表示开放注册，
        if ("0".equals(isOpenRegisterStr)) {
            isOpenRegister = false;
        }
        config.isOpenRegister = isOpenRegister;

        // 是否需要短信验证码，
        String isOpenSMSCodeStr = configBean.getIsOpenSMSCode();
        boolean isOpenSMSCode = false;
        // 为0表示不需要短信验证码，为1表示需要短信验证码，
        if ("1".equals(isOpenSMSCodeStr)) {
            isOpenSMSCode = true;
        }
        config.isOpenSMSCode = isOpenSMSCode;

        config.isQestionOpen = configBean.getIsQestionOpen() == 1 ? true : false;
        // 是否需要邀请码，
        config.registerInviteCode = configBean.getRegisterInviteCode();

        // 是否允许昵称搜索
        config.cannotSearchByNickName = configBean.getNicknameSearchUser() == 0;

        // 是否使用用户名注册，
        config.registerUsername = configBean.getRegeditPhoneOrName();

        config.ordinaryUserCannotSearchFriend = configBean.getIsCommonFindFriends() > 0;
        config.ordinaryUserCannotCreateGroup = configBean.getIsCommonCreateGroup() > 0;

        config.companyName = configBean.getCompanyName();
        config.copyright = configBean.getCopyright();
        config.website = configBean.getWebsite();
        config.headBackgroundImg = configBean.getHeadBackgroundImg();

        config.androidAppUrl = configBean.getAndroidAppUrl();
        config.androidVersion = configBean.getAndroidVersion();

        config.isHideSearchFriend = configBean.getHideSearchByFriends() == 0;

        config.displayRedPacket = configBean.getDisplayRedPacket() == 0;

        config.disableLocationServer = configBean.getIsOpenPositionService() > 0;

        config.enableGoogleFcm = configBean.getIsOpenGoogleFCM() > 0;

        config.popularAPP = configBean.getPopularAPPBean();

        config.XMPPHost = configBean.getXMPPHost();

        config.XMPPDomain = configBean.getXMPPDomain();

        config.xmppPingTime = configBean.getXmppPingTime();

        config.XMPPTimeOut = configBean.getXMPPTimeout();

        config.JitsiServer = configBean.getJitsiServer();
        config.nodesInfoList = configBean.getNodesInfoList();
        config.isNodesStatus = configBean.getIsNodesStatus();

        config.getTabBarConfigList = configBean.getTabBarConfigList();

        config.isUserSignRedPacket = configBean.getIsUserSignRedPacket();
        config.isWithdrawToAdmin = configBean.getIsWithdrawToAdmin();
        config.minWithdrawToAdmin = TextUtils.isEmpty(configBean.getMinWithdrawToAdmin()) ? 0.0 : Double.valueOf(configBean.getMinWithdrawToAdmin());

        config.IS_OPEN_TWO_BAR_CODE = configBean.getIsOpenTwoBarCode();
        config.IS_OPEN_TEL_NUM = configBean.getIsOpenTelnum();

        config.IS_OPEN_OBS_STATUS = configBean.getIsOpenOSStatus();
        config.OBS_OS_TYPE = configBean.getOsType();
        if (config.IS_OPEN_OBS_STATUS == 1) {
            if (config.OBS_OS_TYPE == HUIWEI_OBS) {
                //开启了obs 赋值 obs 配置信息
                config.IS_OPEN_OBS_STATUS = HUIWEI_OBS;
                setObsInfo(configBean, config);
            } else if (config.OBS_OS_TYPE == TENTENT_COS) {
                config.IS_OPEN_OBS_STATUS = TENTENT_COS;
                setObsInfo(configBean, config);
            }
        } else {
            config.IS_OPEN_OBS_STATUS = 0;
        }
        config.wechatAppId = configBean.getWechatAppId();
        config.wechatPayStatus = configBean.getWechatPayStatus();//是否支持微信充值
        config.wechatWithdrawStatus = configBean.getWechatWithdrawStatus();//是否支持微信提现
        config.aliPayStatus = configBean.getAliPayStatus();//是否支持支付宝充值
        config.aliWithdrawStatus = configBean.getAliWithdrawStatus();//是否支持支付宝提现
        config.bankPayStatus = configBean.getYunPayStatus();//是否支持银行卡充值
        config.bankPayStatus2 = configBean.getIsYunPayOpen();//是否支持银行卡充值
        config.transferRate = configBean.getTransferRate();//费率
        config.qqLoginStatus = configBean.getQqLoginStatus();//是否支持QQ登录
        config.wechatLoginStatus = configBean.getWechatLoginStatus();//是否支持微信登录
        config.qqLoginAppId = configBean.getQqLoginAppId();
        config.hmPayStatus = configBean.getHmPayStatus();//黑马充值1开启 2关闭
        config.hmWithdrawStatus = configBean.getHmWithdrawStatus();//黑马提现1开启  2关闭
        config.isWeiBaoStatus = configBean.getIsWeiBaoStatus();//是不是开通云钱包1开通
        config.weiBaoMaxTransferAmount = configBean.getWeiBaoMaxTransferAmount();//微钱包最大提现金额
        config.weiBaoMaxRedPacketAmount = configBean.getWeiBaoMaxRedPacketAmount();//微红包最大金额
        config.weiBaoMinTransferAmount = configBean.getWeiBaoMinTransferAmount();//微钱包最小提现金额
        config.weiBaoTransferRate = configBean.getWeiBaoTransferRate();//微提现费率
        config.isDelAfterReading = configBean.getIsDelAfterReading();//是否开启阅后即焚
        config.isAudioStatus = configBean.getIsAudioStatus();//是否开启音视频
        config.maxSendRedPagesAmount  = configBean.getMaxSendRedPagesAmount();//群红包最大金额
        config.isEnableCusServer = configBean.getIsEnableCusServer();//是否开启联系客服
        config.cusServerUrl = configBean.getCusServerUrl();//联系客服url

        // apiUrl
        initApiUrls(config);
        initOthersUrls(config);
        getUrls(config);
        return config;
    }

    private static void setObsInfo(ConfigBean configBean, AppConfig config) {
        if (TextUtils.isEmpty(configBean.getAccessKeyId())
                || TextUtils.isEmpty(configBean.getAccessSecretKey())
                || TextUtils.isEmpty(configBean.getBucketName())
                || TextUtils.isEmpty(configBean.getEndPoint())) {
            config.IS_OPEN_OBS_STATUS = 0;
        } else {
            if (config.IS_OPEN_OBS_STATUS == TENTENT_COS) { //腾讯的
                config.ACCESS_KEY_ID = configBean.getAccessKeyId();
                config.ACCESS_SECRET_KEY = configBean.getAccessSecretKey();
                config.OBS_BUCKET_NAME = configBean.getBucketName();
                config.OBS_END_POINT = configBean.getEndPoint();
                config.OBS_LOCATION = configBean.getLocation();
                config.OBS_OS_NAME = configBean.getOsName();
                config.OBS_OS_APP_ID = configBean.getOsAppId();
            } else { //华为的
                config.ACCESS_KEY_ID = configBean.getAccessKeyId();
                config.ACCESS_SECRET_KEY = configBean.getAccessSecretKey();
                config.OBS_BUCKET_NAME = configBean.getBucketName();
                config.OBS_END_POINT = configBean.getEndPoint();
                config.OBS_LOCATION = configBean.getLocation();
            }
        }
    }

    private static void initApiUrls(AppConfig config) {
        String apiUrl = config.apiUrl;
        /* 登陆注册 */
        config.USER_LOGIN = apiUrl + "user/login";// 登陆
        config.USER_THIRD_LOGIN_NEW = apiUrl + "user/otherLogin";//新的第三方登陆
        config.OTHER_SET_INVITE_CODE = apiUrl + "user/otherSetInviteCode";//第三方登陆绑定手机号
        config.USER_REGISTER = apiUrl + "user/register";// 注册
        config.USER_PASSWORD_UPDATE = apiUrl + "user/password/update";//  用户修改密码
        config.USER_PASSWORD_RESET = apiUrl + "user/password/reset";// 用户重置密码
        config.QUESTION_CHECK = apiUrl + "question/check";//用户名时候校验密保问题
        config.USER_LOGIN_AUTO = apiUrl + "user/login/auto";// 检测Token是否过期
        config.USER_GETCODE_IMAGE = apiUrl + "getImgCode";// 获取图形验证码
        config.SEND_AUTH_CODE = apiUrl + "basic/randcode/sendSms";// 获取手机验证码
        config.VERIFY_TELEPHONE = apiUrl + "verify/telephone";// 验证手机号有没有被注册
        /* 用户 */
        config.USER_UPDATE = apiUrl + "user/update";// 用户资料修改
        config.USER_GET_URL = apiUrl + "user/get";  // 获取用户资料，更新本地数据的接口
        config.USER_PHOTO_LIST = apiUrl + "user/photo/list";// 获取相册列表
        config.USER_QUERY = apiUrl + "user/query";// 查询用户列表
        config.USER_NEAR = apiUrl + "nearby/user";// 查询搜索列表
        config.PUBLIC_SEARCH = apiUrl + "public/search/list";// 公众号搜索列表
        config.USER_SET_PRIVACY_SETTING = apiUrl + "/user/settings/update";// 设置用户隐私设置
        config.USER_GET_PRIVACY_SETTING = apiUrl + "/user/settings";// 查询用户隐私设置

        config.USER_GET_BAND_ACCOUNT = apiUrl + "/user/getBindInfo";// 查询用户绑定设置
        config.USER_UN_BAND_ACCOUNT = apiUrl + "/user/unbind";// 设置用户绑定设置
        config.OTHER_BIND_Phone_Pass_Word = apiUrl + "user/otherBindPhonePassWord";
        config.AUTHOR_CHECK = apiUrl + "open/codeAuthorCheck";// 授权弹窗url

        config.USER_GET_PUBLIC_MENU = apiUrl + "public/menu/list";// 获取公众号菜单，需要userType = 2 才能获取
        config.USER_DEL_CHATMESSAGE = apiUrl + "tigase/deleteMsg";// 删除某条聊天记录

        config.USER_ADD_COURSE = apiUrl + "user/course/add";     // 添加课程
        config.USER_QUERY_COURSE = apiUrl + "user/course/list";  // 查询课程
        config.USER_EDIT_COURSE = apiUrl + "user/course/update"; // 修改课程
        config.USER_DEL_COURSE = apiUrl + "user/course/delete";  // 删除课程
        config.USER_COURSE_DATAILS = apiUrl + "user/course/get"; // 获取课程

        /* 附近 */
        config.NEARBY_USER = apiUrl + "nearby/user";// 获取附近的用户

        /* 商务圈 */
        config.APP_DISCOVER_LIST = apiUrl + "console/appDiscoverList";//获取发现页信息
        config.USER_CIRCLE_MESSAGE = apiUrl + "b/circle/msg/user/ids";// 获取某个人的商务圈消息
        config.DOWNLOAD_CIRCLE_MESSAGE = apiUrl + "b/circle/msg/ids"; // 下载商务圈消息

        /* 好友 */
        config.FRIENDS_ATTENTION_LIST = apiUrl + "friends/attention/list";// 获取关注列表
        config.FRIENDS_BLACK_LIST = apiUrl + "friends/blacklist";// 获取黑名单列表

        config.FRIENDS_ATTENTION_ADD = apiUrl + "friends/attention/add";// 加关注 || 直接成为好友
        config.ADD_FRIENDS = apiUrl + "friends/add";// 同意成为好友
        config.FRIENDS_BLACKLIST_ADD = apiUrl + "friends/blacklist/add";// 拉黑
        config.FRIENDS_BLACKLIST_DELETE = apiUrl + "friends/blacklist/delete";// 取消拉黑
        config.FRIENDS_ATTENTION_DELETE = apiUrl + "friends/attention/delete";// 取消关注
        config.FRIENDS_DELETE = apiUrl + "friends/delete";// 删除好友

        config.FRIENDS_REMARK = apiUrl + "friends/remark";// 备注好友
        config.FRIENDS_UPDATE = apiUrl + "friends/update";// 消息保存天数
        config.FRIENDS_NOPULL_MSG = apiUrl + "friends/update/OfflineNoPushMsg";// 消息免打扰

        /* 群聊 */
        config.ROOM_ADD = apiUrl + "room/add";// 创建群组
        config.ROOM_JOIN = apiUrl + "room/join";// 加入群组
        config.ROOM_MEMBER_UPDATE = apiUrl + "room/member/update";// 添加成员
        config.ROOM_DELETE = apiUrl + "room/delete";// 删除群组
        config.ROOM_MEMBER_DELETE = apiUrl + "room/member/delete";// 删除成员 || 退出群组
        config.ROOM_MEMBERS_DELETE = apiUrl + "room/members/delete";//批量删除群成员
        config.ROOM_TRANSFER = apiUrl + "room/transfer";// 转让群组

        config.ROOM_GET = apiUrl + "room/get";// 获取群组
        config.ROOM_LIST = apiUrl + "room/list";// 获取群组列表
        config.ROOM_MEMBER_GET = apiUrl + "room/member/get";// 获取成员
        config.ROOM_MEMBER_LIST = apiUrl + "room/member/getMemberListByPage";// 获取成员列表
        config.ROOM_LIST_HIS = apiUrl + "room/list/his";// 获取某个用户已加入的房间列表
        config.ROOM_GET_ROOM = apiUrl + "room/getRoom";// 获取自己的成员信息以及群属性
        config.ROOM_UPDATE = apiUrl + "room/update";// 设置群组
        config.ROOM_MANAGER = apiUrl + "room/set/admin";// 指定管理员
        config.ROOM_UPDATE_ROLE = apiUrl + "room/setInvisibleGuardian";// 指定隐身人，监控人，
        config.ROOM_DELETE_NOTICE = apiUrl + "room/notice/delete";// 删除群公告
        config.ROOM_LOCATION_QUERY = apiUrl + "room/location/query";// 面对面建群查询
        config.ROOM_LOCATION_JOIN = apiUrl + "room/location/join";// 面对面建群加入
        config.ROOM_LOCATION_EXIT = apiUrl + "room/location/exit";// 面对面建群退出

        config.ROOM_DISTURB = apiUrl + "room/member/setOfflineNoPushMsg";// 消息免打扰

        config.OPEN_GET_HELPER_LIST = apiUrl + "open/getHelperList";// 添加群助手
        config.ROOM_ADD_GROUP_HELPER = apiUrl + "room/addGroupHelper";// 添加群助手
        config.ROOM_DELETE_GROUP_HELPER = apiUrl + "room/deleteGroupHelper";// 移除群助手
        config.ROOM_QUERY_GROUP_HELPER = apiUrl + "room/queryGroupHelper";// 查询群助手
        config.ROOM_ADD_AUTO_RESPONSE = apiUrl + "room/addAutoResponse";// 添加自动回复
        config.ROOM_UPDATE_AUTO_RESPONSE = apiUrl + "room/updateAutoResponse";// 修改自动回复
        config.ROOM_DELETE_AUTO_RESPONSE = apiUrl + "room/deleteAutoResponse";// 删除自动回复
        config.ROOM_GROUP_SIGN = apiUrl + "room/signInRightNow";// 群签到
        config.ROOM_GROUP_SIGN_DETAIL = apiUrl + "room/signInDetails";// 群签到个人详情
        config.ROOM_GROUP_SIGN_REWARD = apiUrl + "room/exchangeGift";// 群签兑换奖励
        config.ROOM_GROUP_SIGN_RECORD = apiUrl + "room/signInDetailsRoom";// 群签到列表
        config.ROOM_GROUP_INFO_BY_MONTH = apiUrl + "room/getSignInDate";// 群签日历
        config.ROOM_GROUP_QRCODE_SHARE = apiUrl + "appQCCodeShare?roomId=";// 群签日历

        /* 抖音 */
        config.GET_TRILL_LIST = apiUrl + "b/circle/msg/pureVideo";// 获取抖音视频列表
        config.GET_MUSIC_LIST = apiUrl + "music/list";// 获取抖音音乐列表

        /* 直播 */
        config.GET_LIVE_ROOM_LIST = apiUrl + "liveRoom/list";// 获取直播间列表
        config.CREATE_LIVE_ROOM = apiUrl + "liveRoom/create";// 创建直播间
        config.JOIN_LIVE_ROOM = apiUrl + "liveRoom/enterInto";// 加入直播间
        config.EXIT_LIVE_ROOM = apiUrl + "liveRoom/quit";// 退出直播间
        config.DELETE_LIVE_ROOM = apiUrl + "liveRoom/delete";// 删除直播间
        config.LIVE_ROOM_DETAIL = apiUrl + "liveRoom/get";// 直播间详情
        config.LIVE_ROOM_MEMBER_LIST = apiUrl + "liveRoom/memberList";// 获取直播间成员列表
        config.LIVE_ROOM_DANMU = apiUrl + "liveRoom/barrage";// 发送弹幕
        config.GET_LIVE_GIFT_LIST = apiUrl + "liveRoom/giftlist";// 得到礼物列表
        config.LIVE_ROOM_GIFT = apiUrl + "liveRoom/give";// 发送礼物

        config.LIVE_ROOM_PRAISE = apiUrl + "liveRoom/praise";// 发送点赞

        config.LIVE_ROOM_GET_IDENTITY = apiUrl + "liveRoom/get/member";// 获取身份信息
        config.LIVE_ROOM_UPDATE = apiUrl + "liveRoom/update";// 修改直播间
        config.LIVE_ROOM_SET_MANAGER = apiUrl + "liveRoom/setmanage";// 设置管理员
        config.LIVE_ROOM_SHUT_UP = apiUrl + "liveRoom/shutup";// 禁言/取消禁言
        config.LIVE_ROOM_KICK = apiUrl + "liveRoom/kick";// 踢人
        config.LIVE_ROOM_STATE = apiUrl + "liveRoom/start";// 正在直播 || 未开启直播
        config.LIVE_GET_LIVEROOM = apiUrl + "liveRoom/getLiveRoom";// 获取自己的直播间

        config.EMPTY_SERVER_MESSAGE = apiUrl + "tigase/emptyMyMsg";// 清空与某人的聊天记录

        /* 商务圈 */
        config.MSG_WITH_CONDITION  =apiUrl+"b/circle/msg/getMsgWithCondition";//获取某个用户的朋友圈
        config.MSG_ADD_URL = apiUrl + "b/circle/msg/add";// 发布一条公共消息的接口
        config.MSG_LIST = apiUrl + "b/circle/msg/list";// 获取公共消息的接口
        config.MSG_USER_LIST = apiUrl + "b/circle/msg/user";// 获取某个用户的最新公共消息
        config.MSG_GETS = apiUrl + "b/circle/msg/gets";// 根据IDS批量获取公共消息的接口(我的商务圈使用)
        config.MSG_GET = apiUrl + "b/circle/msg/get";// 根据ID获取公共消息
        config.CIRCLE_MSG_DELETE = apiUrl + "b/circle/msg/delete";// 删除一条商务圈消息
        config.MSG_PRAISE_ADD = apiUrl + "b/circle/msg/praise/add";// 赞
        config.MSG_PRAISE_DELETE = apiUrl + "b/circle/msg/praise/delete";// 取消赞
        config.MSG_PRAISE_LIST = apiUrl + "b/circle/msg/praise/list";// 点赞分页加载的接口，
        config.MSG_COLLECT_DELETE = apiUrl + "/b/circle/msg/deleteCollect";// 删除收藏
        config.CIRCLE_MSG_LATEST = apiUrl + "b/circle/msg/latest";// 最新人才榜
        config.CIRCLE_MSG_HOT = apiUrl + "b/circle/msg/hot";// 最热人才榜
        config.MSG_COMMENT_ADD = apiUrl + "b/circle/msg/comment/add";// 增加一条评论
        config.MSG_COMMENT_DELETE = apiUrl + "b/circle/msg/comment/delete";// 删除一条评论
        config.MSG_COMMENT_LIST = apiUrl + "b/circle/msg/comment/list";//返回评论列表
        config.UPLOAD_MUC_FILE_ADD = apiUrl + "room/add/share";// 上传群文件
        config.UPLOAD_MUC_FILE_FIND_ALL = apiUrl + "room/share/find";// 查询所有群文件
        config.UPLOAD_MUC_FILE_FIND = apiUrl + "room/share/find";// 查询单个群文件
        config.UPLOAD_MUC_FILE_DEL = apiUrl + "room/share/delete";// 删除单个群文件

        /* 登出 */
        config.USER_LOGOUT = apiUrl + "user/logout";
        config.USER_OUTTIME = apiUrl + "user/outtime";

        /* 红包相关的URL*/
        config.REDPACKET_SEND = apiUrl + "redPacket/sendRedPacket/v1"; // 发送红包
        config.REDPACKET_OPEN = apiUrl + "redPacket/openRedPacket"; // 打开红包
        config.RECIVE_REDPACKET_LIST_GET = apiUrl + "redPacket/getRedReceiveList"; // 获得接收的红包
        config.SEND_REDPACKET_LIST_GET = apiUrl + "redPacket/getSendRedPacketList";// 获得发送的红包
        config.RENDPACKET_GET = apiUrl + "redPacket/getRedPacket"; // 获得红包详情
        config.RENDPACKET_REPLY = apiUrl + "redPacket/reply"; // 回复红包
        config.TRANSACTION_RECORD = apiUrl + "friend/consumeRecordList";// 好友交易记录明细
        config.CONSUMERECORD_GET = apiUrl + "user/consumeRecord/list"; // 获取消费记录列表
        config.RECHARGE_ADD = apiUrl + "user/Recharge";// 余额充值
        config.RECHARGE_GET = apiUrl + "user/getUserMoeny";// 余额查询

        config.REDPACKET_CREATE = apiUrl + "webox/redpacket/create";//发云红包
        config.REDPACKET_CREATE_INQUIRE = apiUrl + "webox/redpacket/query";//查询红包状态
        config.REDPACKET_GRAD = apiUrl + "webox/redpacket/grab";//抢云红包
        config.CLOUD_PROTOCOL = BuildConfig.PROTOCOL_URL + "/privacyClause.html";//云红包协议
        config.MICRO_TRANSFER_SEND_TRANSFER = apiUrl + "webox/transfer/create";//微转账预下单
        config.MICRO_RECHARGE_GET = apiUrl + "webox/transfer/query";//查询微转账
        config.MICRO_RECHARGE_ACCEPT = apiUrl + "webox/transfer/confirm";//接受微转账

        config.TRANSFER_SEND_TRANSFER = apiUrl + "tigTransfer/sendTransfer";// 转账
        config.TRANSFER_GET_TRANSFERINFO = apiUrl + "tigTransfer/getTransferInfo";// 获取转账信息
        config.TRANSFER_RECEIVE_TRANSFER = apiUrl + "tigTransfer/receiveTransfer";// 接受转账
        config.PAY_CODE_PAYMENT = apiUrl + "pay/codePayment";// 向展示付款码的用户收钱
        config.PAY_CODE_RECEIPT = apiUrl + "pay/codeReceipt";// 向展示收款码的用户付钱

        config.VX_RECHARGE = apiUrl + "user/recharge/getSign";// 微信/支付宝充值
        config.VX_UPLOAD_CODE = apiUrl + "user/bind/wxcode";  // 上传用户Code
        config.VX_GET_OPEN_ID = apiUrl + "user/getWxOpenId";  // 上传用户Code
        config.BAND_THIRD_PARTY_NEW = apiUrl + "user/otherBindUserInfo";//绑定微信新版
        config.VX_TRANSFER_PAY = apiUrl + "transfer/wx/pay";  // 微信取现
        config.ALIPAY_AUTH = apiUrl + "user/bind/getAliPayAuthInfo";  // 支付宝授权
        config.ALIPAY_BIND = apiUrl + "user/bind/aliPayUserId";  // 支付宝绑定
        config.ALIPAY_TRANSFER = apiUrl + "alipay/transfer";  // 支付宝取现

        config.PAY_GET_ORDER_INFO = apiUrl + "pay/getOrderInfo";  // 获取订单信息
        config.PAY_PASSWORD_PAYMENT = apiUrl + "pay/passwordPayment";  // 输入密码后支付接口

        /* 组织架构相关 */
        config.AUTOMATIC_SEARCH_COMPANY = apiUrl + "org/company/getByUserId";// 自动查找公司
        config.CREATE_COMPANY = apiUrl + "org/company/create";// 创建公司
        config.SET_COMPANY_MANAGER = apiUrl + "org/company/setManager";// 指定管理者
        config.MODIFY_COMPANY_NAME = apiUrl + "org/company/modify";// 修改公司名称
        config.CHANGE_COMPANY_NOTIFICATION = apiUrl + "org/company/changeNotice";// 更改公司公告
        config.SEARCH_COMPANY = apiUrl + "org/company/search";// 查找公司
        config.DELETE_COMPANY = apiUrl + "org/company/delete";// 删除公司
        config.CREATE_DEPARTMENT = apiUrl + "org/department/create";// 创建部门
        config.MODIFY_DEPARTMENT_NAME = apiUrl + "org/department/modify";// 修改部门名称
        config.DELETE_DEPARTMENT = apiUrl + "org/department/delete";// 删除部门
        config.ADD_EMPLOYEE = apiUrl + "org/employee/add";// 添加员工
        config.DELETE_EMPLOYEE = apiUrl + "org/employee/delete"; // 删除员工
        config.MODIFY_EMPLOYEE_DEPARTMENT = apiUrl + "org/employee/modifyDpart";// 更改员工部门
        config.COMPANY_LIST = apiUrl + "org/company/list";// 公司列表
        config.DEPARTMENT_LIST = apiUrl + "org/department/list"; // 部门列表
        config.EMPLOYEE_LIST = apiUrl + "org/employee/list";// 员工列表
        config.GET_COMPANY_DETAIL = apiUrl + "org/company/get";// 获取公司详情
        config.GET_DEPARTMENT_DETAIL = apiUrl + "org/employee/get";// 获取员工详情
        config.GET_EMPLOYEE_DETAIL = apiUrl + "org/dpartment/get"; // 获取部门详情
        config.GET_EMPLOYEE_NUMBER_OF_COMPANY = apiUrl + "org/company/empNum"; // 获取公司员工人数
        config.EXIT_COMPANY = apiUrl + "org/company/quit";// 获取部门员工人数
        config.CHANGE_EMPLOYEE_IDENTITY = apiUrl + "org/employee/modifyPosition";// 修改职位

        /* 标签相关 */
        config.FRIENDGROUP_LIST = apiUrl + "friendGroup/list";
        config.FRIENDGROUP_ADD = apiUrl + "friendGroup/add";
        config.FRIENDGROUP_DELETE = apiUrl + "friendGroup/delete";
        config.FRIENDGROUP_UPDATE = apiUrl + "friendGroup/update";
        config.FRIENDGROUP_UPDATEGROUPUSERLIST = apiUrl + "friendGroup/updateGroupUserList";
        config.FRIENDGROUP_UPDATEFRIEND = apiUrl + "friendGroup/updateFriend";

        /* 漫游 */
        config.GET_LAST_CHAT_LIST = apiUrl + "tigase/getLastChatList";
        config.GET_CHAT_MSG = apiUrl + "tigase/tig_msgs";
        config.GET_CHAT_MSG_MUC = apiUrl + "tigase/tig_muc_msgs";

        /* 收藏相关 */
        config.Collection_ADD = apiUrl + "user/emoji/add"; // 添加收藏
        config.Collection_REMOVE = apiUrl + "user/emoji/delete"; // 删除收藏
        config.Collection_LIST = apiUrl + "user/emoji/list"; // 表情列表
        config.Collection_LIST_OTHER = apiUrl + "user/collection/list"; // 收藏列表

        config.USER_REPORT = apiUrl + "user/report";// 举报用户 || 群组
        config.UPLOAD_COPY_FILE = apiUrl + "upload/copyFile";// 服务端将文件拷贝一份，更换另外一个url地址返回
        config.ADDRESSBOOK_UPLOAD = apiUrl + "addressBook/upload";// 上传本地联系人
        config.ADDRESSBOOK_GETALL = apiUrl + "addressBook/getAll";// 查询通讯录好友
        config.ADDENTION_BATCH_ADD = apiUrl + "friends/attention/batchAdd";// 联系人内加好友 不需要验证

        config.CHECK_PAY_PASSWORD = apiUrl + "user/checkPayPassword"; // 检查支付密码，
        config.UPDATE_PAY_PASSWORD = apiUrl + "user/update/payPassword"; // 修改支付密码，
        config.QUESTION_LIST = apiUrl + "question/list";//获取密保问题
        config.QUESTION_SET = apiUrl + "question/set";//设置密保问题
        //TODO  临时授权地址需要更改
        config.GET_COS_KEY = apiUrl + ""; //获取腾讯Cos 临时授权信息
        config.GET_NEW_VERSION = apiUrl + "newVersion"; //获取版本信息
        config.LOGIN_PUBLIC_NUMBER = apiUrl + "mp/loginPublicAcc";// 公众号扫码登录
        config.LOGIN_PC = apiUrl + "user/login/scan"; //扫码登录pc
        config.LOGIN_PUBLIC_OPEN_NUMBER = apiUrl + "open/loginPublicOpenAcc";
        config.AUTHORIZATION = apiUrl + "open/authorization";//验证第三方账号是否开通
        config.CODE_OAUTH = apiUrl + "open/code/oauth";//根据Code获取权限
        config.MEDIA_STARTUP = apiUrl + "basic/media/startup";
        // 集群 获取meetUrl
        config.OPEN_MEET = apiUrl + "user/openMeet";
        config. POINTS_ASSISTANT = apiUrl+"room/signInGroup";//积分小助手

        // 小米推送
        config.configMi = apiUrl + "user/xmpush/setRegId";
        // 华为推送
        config.configHw = apiUrl + "user/hwpush/setToken";
        // 极光推送
        config.configJg = apiUrl + "user/jPush/setRegId";
        // vivo推送接口
        config.configVi = apiUrl + "user/VIVOPush/setPushId";
        // oppo推送接口
        config.configOp = apiUrl + "user/OPPOPush/setPushId";
        // firebase推送接口
        config.configFcm = apiUrl + "user/fcmPush/setToken";
        config.configFcm = apiUrl + "user/fcmPush/setToken";
        // 魅族推送接口
        config.configMz = apiUrl + "user/MZPush/setPushId";

        config.SDK_OPEN_AUTH_INTERFACE = apiUrl + "open/authInterface";
        // 获取服务器时间接口，用于校准时间，
        config.GET_CURRENT_TIME = apiUrl + "getCurrentTime";
        config.URL_CHECK = apiUrl + "user/checkReportUrl";

        //签到
        config.GET_USER_SIGN_INFO_BY_MONTH = apiUrl + "extend/getUserSignDateByMonth";//获取用户某月签到信息
        config.SIGN_IN_NOW = apiUrl + "extend/signIn";//立即签到
        config.GET_USER_SIGN_INFO = apiUrl + "extend/getUserSignDateByWeek";//获取用户签到信息
        config.TI_XIAN = apiUrl + "user/transferToAdmin";//提现到后台
        //支付密码
        config.Pay_Forgot_PassWord = apiUrl + "user/forget/password";

        /* 表情*/
        config.LOAD_BQ_SHOP_LIST = apiUrl + "user/emojiUserStoreList/page"; //获取上店表情包
        config.ADD_BQ = apiUrl + "user/emojiUserList/add"; //用户添加表情
        config.LOAD_MY_BQ = apiUrl + "user/emojiUserList/page";//用户添加的表情列表
        config.REMOVE_MY_BQ = apiUrl + "user/emojiUserList/delete";


        config.GET_BANK_LIST = apiUrl + "pay/getPayMethod";
        config.SUBMIT_BANK_INFO_PAY = apiUrl + "pay/getOrderDetails";

        config.GET_MY_BANK_CARD_LIST = apiUrl + "bank/getBankInfoByUserId";

        config.ADD_BANK_CARD = apiUrl + "bank/userBindBandInfo";
        config.DEL_MY_BANK_CARD = apiUrl + "bank/delBankInfoById";
        config.LOG_REPORT = apiUrl + "logReport";//异常上报
        config.CLEAR_GROUP_CHAT = apiUrl + "tigase/emptyMyMsg";
        config.DELETE_MSG_LIST_ONE = apiUrl + "tigase/deleteOneLastChat";
        config.TWO_WAY_WITHDRAWAL = apiUrl + "tigase/delectRoomMsg";

        /*
         * 银卡操作
         * */
        config.WITHDRAW_METHOD_SET = apiUrl + "user/withdrawMethodSet";
        config.DELETE_BLANCE = apiUrl + "user/withdrawMethodDelete";
        config.LIST_BLANCE = apiUrl + "user/withdrawMethodGet";
        config.GET_WITHDRAW_WAY = apiUrl + "user/getWithdrawWay";


    }


    private static void initOthersUrls(AppConfig config) {
        String uploadUrl = config.uploadUrl;
        String dowmLoadUrl = config.downloadAvatarUrl;
        Log.e("dowmLoadUrl", "initOthersUrls: " + dowmLoadUrl);

        config.UPLOAD_URL = uploadUrl + "upload/UploadServlet";// 上传图片接口
        config.AVATAR_UPLOAD_URL = uploadUrl + "upload/UploadAvatarServlet";// 上传头像接口
        config.ROOM_UPDATE_PICTURE = uploadUrl + "upload/GroupAvatarServlet";
        // downloadAvatarUrl
        config.AVATAR_ORIGINAL_PREFIX = dowmLoadUrl + "avatar/o";// 头像原图前缀地址
        config.AVATAR_THUMB_PREFIX = dowmLoadUrl + "avatar/t";// 头像缩略图前缀地址
    }

    private static void getUrls(AppConfig config) {

        /*判断字符ab在字符str中出现的次数*/
        // 需要对比的源字符串
        String str = config.apiUrl;
        // 需要对比的字符串
        String compareStr = ":";
        //字符串查找初始从0开始查找
        int indexStart = 0;
        //获取查找字符串的长度，这里有个规律：第二次查找出字符串的起始位置等于 第一次ab字符串出现的位置+ab的长度
        int compareStrLength = compareStr.length();
        int count = 0;
        while (true) {
            int tm = str.indexOf(compareStr, indexStart);
            if (tm >= 0) {
                count++;
                //  没查找一次就从新计算下次开始查找的位置
                indexStart = tm + compareStrLength;
            } else {
                //直到没有匹配结果为止
                break;
            }
        }

        if (config.apiUrl.endsWith("/")) {
            if (count > 1) {
                config.second_channel_url = config.apiUrl.substring(0, config.apiUrl.lastIndexOf(":"));
            } else {
                config.second_channel_url = config.apiUrl.substring(0, config.apiUrl.lastIndexOf("/"));
            }
        } else {
            if (count > 1) {
                config.second_channel_url = config.apiUrl.substring(0, config.apiUrl.lastIndexOf(":"));
            } else {
                config.second_channel_url = config.apiUrl;
            }
        }
    }
}
