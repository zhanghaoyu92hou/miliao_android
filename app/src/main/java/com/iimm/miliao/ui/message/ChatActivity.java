package com.iimm.miliao.ui.message;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.iimm.miliao.ui.me.LocalVideoActivity;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.adapter.MessageEventClickable;
import com.iimm.miliao.adapter.MessageEventRequert;
import com.iimm.miliao.adapter.MessageLocalVideoFile;
import com.iimm.miliao.adapter.MessageUploadChatRecord;
import com.iimm.miliao.adapter.MessageVideoFile;
import com.iimm.miliao.audio_x.VoicePlayer;
import com.iimm.miliao.bean.Contacts;
import com.iimm.miliao.bean.EventBusMsg;
import com.iimm.miliao.bean.EventTransfer;
import com.iimm.miliao.bean.EventUploadCancel;
import com.iimm.miliao.bean.EventUploadFileRate;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.PrivacySetting;
import com.iimm.miliao.bean.PublicMenu;
import com.iimm.miliao.bean.User;
import com.iimm.miliao.bean.VideoFile;
import com.iimm.miliao.bean.assistant.GroupAssistantDetail;
import com.iimm.miliao.bean.collection.CollectionEvery;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.bean.message.ChatRecord;
import com.iimm.miliao.bean.redpacket.CloudQueryRedPacket;
import com.iimm.miliao.bean.redpacket.EventCloudRedReceived;
import com.iimm.miliao.bean.redpacket.EventRedReceived;
import com.iimm.miliao.bean.redpacket.OpenRedpacket;
import com.iimm.miliao.bean.redpacket.RedDialogBean;
import com.iimm.miliao.bean.redpacket.RedPacket;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.broadcast.OtherBroadcast;
import com.iimm.miliao.call.Jitsi_connecting_second;
import com.iimm.miliao.call.Jitsi_pre;
import com.iimm.miliao.call.MessageEventClicAudioVideo;
import com.iimm.miliao.call.MessageEventSipEVent;
import com.iimm.miliao.call.MessageEventSipPreview;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.VideoFileDao;
import com.iimm.miliao.downloader.Downloader;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.PrivacySettingHelper;
import com.iimm.miliao.helper.UploadEngine;
import com.iimm.miliao.pay.MicroTransferMoneyActivity;
import com.iimm.miliao.pay.TransferMoneyActivity;
import com.iimm.miliao.pay.TransferMoneyDetailActivity;
import com.iimm.miliao.ui.MainActivity;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.ui.contacts.SendContactsActivity;
import com.iimm.miliao.ui.dialog.CreateCourseDialog;
import com.iimm.miliao.ui.emoji.BqShopActivity;
import com.iimm.miliao.ui.emoji.CustomBqManageActivity;
import com.iimm.miliao.ui.map.MapPickerActivity;
import com.iimm.miliao.ui.me.MyCollection;
import com.iimm.miliao.ui.me.redpacket.CloudRedDetailsActivity;
import com.iimm.miliao.ui.me.redpacket.RedDetailsActivity;
import com.iimm.miliao.ui.me.redpacket.SendCloudRedPacketActivity;
import com.iimm.miliao.ui.me.redpacket.SendRedPacketActivity;
import com.iimm.miliao.ui.message.single.PersonSettingActivity;
import com.iimm.miliao.ui.mucfile.XfileUtils;
import com.iimm.miliao.ui.other.BasicInfoActivity;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DisplayUtil;
import com.iimm.miliao.util.FileUtil;
import com.iimm.miliao.util.HtmlUtils;
import com.iimm.miliao.util.HttpUtil;
import com.iimm.miliao.util.ImageUtils;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.StringUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.util.log.FileUtils;
import com.iimm.miliao.util.log.LogUtils;
import com.iimm.miliao.util.permission.AndPermissionUtils;
import com.iimm.miliao.util.permission.OnPermissionClickListener;
import com.iimm.miliao.video.MessageEventGpu;
import com.iimm.miliao.video.VideoRecorderActivity;
import com.iimm.miliao.view.ChatBottomView;
import com.iimm.miliao.view.ChatBottomView.ChatBottomListener;
import com.iimm.miliao.view.ChatFaceView;
import com.iimm.miliao.view.NoDoubleClickListener;
import com.iimm.miliao.view.SelectCardPopupWindow;
import com.iimm.miliao.view.SelectFileDialog;
import com.iimm.miliao.view.SelectionFrame;
import com.iimm.miliao.view.mucChatHolder.AChatHolderInterface;
import com.iimm.miliao.view.mucChatHolder.MessageEventClickFire;
import com.iimm.miliao.view.mucChatHolder.MucChatContentView;
import com.iimm.miliao.view.mucChatHolder.MucChatContentView.MessageEventListener;
import com.iimm.miliao.view.mucChatHolder.ReadDelManager;
import com.iimm.miliao.view.photopicker.PhotoPickerActivity;
import com.iimm.miliao.view.photopicker.SelectModel;
import com.iimm.miliao.view.photopicker.intent.PhotoPickerIntent;
import com.iimm.miliao.view.redDialog.RedDialog;
import com.iimm.miliao.xmpp.ListenerManager;
import com.iimm.miliao.xmpp.listener.ChatMessageListener;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JVCideoPlayerStandardforchat;
import fm.jiecao.jcvideoplayer_lib.MessageEvent;
import okhttp3.Call;
import pl.droidsonroids.gif.GifDrawable;
import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;


/**
 * 单聊界面
 */
public class ChatActivity extends BaseActivity implements
        MessageEventListener, ChatBottomListener, ChatMessageListener,
        SelectCardPopupWindow.SendCardS {

    private String TAG = "ChatActivity";
    public static final String FRIEND = "friend";
    /*输入红包金额的返回*/
    public static final int REQUEST_CODE_SEND_RED = 13;     // 发红包
    public static final int REQUEST_CODE_SEND_RED_PT = 10;  // 普通红包返回
    public static final int REQUEST_CODE_SEND_RED_KL = 11;  // 口令红包返回
    public static final int REQUEST_CODE_SEND_RED_PSQ = 12; // 拼手气红包返回

    public static final int REQUEST_CODE_SEND_RED_CLOUD = 14;//云红包
    public static final int REQUEST_CODE_SEND_RED_PT_CLOUD = 10;  // 云红包返回
    // 发送联系人，
    public static final int REQUEST_CODE_SEND_CONTACT = 21;
    /***********************
     * 拍照和选择照片
     **********************/
    private static final int REQUEST_CODE_CAPTURE_PHOTO = 1;
    private static final int REQUEST_CODE_PICK_PHOTO = 2;
    private static final int REQUEST_CODE_SELECT_VIDEO = 3;
    private static final int REQUEST_CODE_SEND_COLLECTION = 4;// 我的收藏 返回
    private static final int REQUEST_CODE_SELECT_LOCATE = 5;
    private static final int REQUEST_CODE_QUICK_SEND = 6;
    private static final int REQUEST_CODE_SELECT_FILE = 7;
    public static final int REQUEST_CODE_INSTANTMESSAGE = 10000;
    RefreshBroadcastReceiver receiver = new RefreshBroadcastReceiver();
    /*******************************************
     * 自动同步其他端收发的消息 && 获取漫游聊天记录
     ******************************************/
    List<ChatMessage> chatMessages;
    @SuppressWarnings("unused")
    private MucChatContentView mChatContentView;
    private SmartRefreshLayout refresh;

    // 存储聊天消息
    private List<ChatMessage> mChatMessages;
    private ChatBottomView mChatBottomView;
    private ImageView mChatBgIv;// 聊天背景
    private AudioManager mAudioManager;
    // 当前聊天对象
    private Friend mFriend;
    private String mLoginUserId;
    private String mLoginNickName;
    private boolean isSearch;
    private double mSearchTime;
    // 消息转发
    private String instantMessage;
    // 是否为通知栏进入
    private boolean isNotificationComing;
    // 我的黑名单列表
    private List<Friend> mBlackList;
    private TextView mTvTitleLeft;
    // 在线 || 离线...
    private TextView mTvTitle;
    // 对方正在输入
    // 是否为阅后即焚
    private int isReadDel;
    private String userId;// 如果isDevice==1，代表当前的聊天对象为我的手机 || 我的电脑，当发消息时，userId要等于自己的id，而非ios || pc...;
    private long mMinId = 0;
    private int mPageSize = 20;
    private boolean mHasMoreData = true;
    private Uri mNewPhotoUri;
    private Map<String, String> packetIdMap = new ConcurrentHashMap<>();
    private HashSet<String> mDelayDelMaps = new HashSet<>();// 记录阅后即焚消息的 packedid
    private ChatMessage replayMessage;
    private RedDialog mRedDialog;
    CountDownTimer time = new CountDownTimer(10000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            String remarkName = mFriend.getRemarkName();
            if (TextUtils.isEmpty(remarkName)) {
                mTvTitle.setText(mFriend.getNickName() + "(" + InternationalizationHelper.getString("JX_OnLine") + ")");
            } else {
                mTvTitle.setText(remarkName + "(" + InternationalizationHelper.getString("JX_OnLine") + ")");
            }
        }
    };
    private UploadEngine.ImFileUploadResponse mUploadResponse = new UploadEngine.ImFileUploadResponse() {
        @Override
        public void onSuccess(String toUserId, ChatMessage message) {
            sendMsg(message);
        }

        @Override
        public void onFailure(String toUserId, ChatMessage message) {
            for (int i = 0; i < mChatMessages.size(); i++) {
                ChatMessage msg = mChatMessages.get(i);
                if (message.get_id() == msg.get_id()) {
                    msg.setMessageState(Constants.MESSAGE_SEND_FAILED);
                    ChatMessageDao.getInstance().updateMessageSendState(mLoginUserId, mFriend.getUserId(),
                            message.get_id(), Constants.MESSAGE_SEND_FAILED);
                    mChatContentView.notifyItemChanged(i);
                    break;
                }
            }
        }
    };
    //
    public static final int REQUEST_MANAGE = 1126;


    public static void start(Context ctx, Friend friend) {
        Intent intent = new Intent(ctx, ChatActivity.class);
        intent.putExtra(ChatActivity.FRIEND, friend);
        ctx.startActivity(intent);
    }
/*
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(AppConstant.EXTRA_FRIEND, mFriend);
    }
*/

    /**
     * 通知聊天页面关闭，
     * 比如被删除被拉黑，
     *
     * @param content 弹Toast的内容，
     */
    public static void callFinish(Context ctx, String content, String toUserId) {
        Intent intent = new Intent();
        intent.putExtra("content", content);
        intent.putExtra("toUserId", toUserId);
        intent.setAction(com.iimm.miliao.broadcast.OtherBroadcast.TYPE_DELALL);
        ctx.sendBroadcast(intent);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("sssss", "onCreate");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_mucchat);
        getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
        /*AndroidBug5497Workaround.assistActivity(this);*/
        mLoginUserId = coreManager.getSelf().getUserId();
        mLoginNickName = coreManager.getSelf().getNickName();
        if (getIntent() != null) {
            mFriend = (Friend) getIntent().getSerializableExtra(AppConstant.EXTRA_FRIEND);
            isSearch = getIntent().getBooleanExtra("isserch", false);
            if (isSearch) {
                mSearchTime = getIntent().getDoubleExtra("jilu_id", 0);
            }
            instantMessage = getIntent().getStringExtra("messageId");
            isNotificationComing = getIntent().getBooleanExtra(Constants.IS_NOTIFICATION_BAR_COMING, false);
        }
        if (mFriend == null) {
            ToastUtil.showToast(this, "获取好友失败");
            return;
        }
        if (mFriend.getIsDevice() == 1) {
            userId = mLoginUserId;
        }
        Log.i("xiaotao","是否开启阅后即焚"+coreManager.getConfig().isDelAfterReading);
        if (coreManager.getConfig().isDelAfterReading != 0 ){
            PreferenceUtils.putInt(mContext, Constants.MESSAGE_READ_FIRE + mFriend.getUserId() + mLoginUserId, 0);
        }
        isReadDel = PreferenceUtils.getInt(mContext, Constants.MESSAGE_READ_FIRE + mFriend.getUserId() + mLoginUserId, 0);
        // mSipManager = SipManager.getInstance();
        mAudioManager = (AudioManager) getSystemService(android.app.Service.AUDIO_SERVICE);
        Downloader.getInstance().init(FileUtil.getFileDir());
        initActionBar();
        initView();
        // 添加新消息来临监听
        ListenerManager.getInstance().addChatMessageListener(this);
        // 注册EventBus
        EventBus.getDefault().register(this);
        // 注册广播
        register();
        if (mFriend.getUserId().equals(Constants.ID_SYSTEM_MESSAGE)) {
            initSpecialMenu();
        } else {
            // 获取聊天对象当前的在线状态
            initFriendState();
        }
        if (Constants.SUPPORT_FORCED_ACQUISITION){
            getNetSingleRecent();
        }

    }

    public void getNetSingleRecent() {
        Map<String, String> params = new HashMap();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("receiver", mFriend.getUserId());
        params.put("startTime", "0");
        params.put("endTime", "0");
        params.put("pageSize", String.valueOf(Constants.MSG_ROMING_PAGE_SIZE));
        params.put("pageIndex", "0");

        HttpUtils.get().url(coreManager.getConfig().GET_CHAT_MSG)
                .params(params)
                .build()
                .execute(new ListCallback<ChatRecord>(ChatRecord.class) {
                    @Override
                    public void onResponse(ArrayResult<ChatRecord> result) {
                        List<ChatRecord> chatRecordList = result.getData();
                        if (chatRecordList != null && chatRecordList.size() > 0) {
                            long currTime = TimeUtils.time_current_time();
                            for (int i = 0; i < chatRecordList.size(); i++) {
                                ChatRecord data = chatRecordList.get(i);
                                String messageBody = data.getBody();
                                messageBody = messageBody.replaceAll("&quot;", "\"");
                                ChatMessage chatMessage = new ChatMessage(messageBody);
                                // 有一种情况，因为服务器1个小时才去删除一次过期消息，所以可能会拉到已过期的时间
                                if (chatMessage.getIsReadDelByInt() > 0 && chatMessage.getDeleteTime() > 1 && chatMessage.getDeleteTime() < currTime) {
                                    // 已过期的消息,扔掉
                                    continue;
                                }
                                if (!TextUtils.isEmpty(chatMessage.getFromUserId()) &&
                                        chatMessage.getFromUserId().equals(mLoginUserId)) {
                                    chatMessage.setMySend(true);
                                }
                                chatMessage.setSendRead(data.getIsRead() > 0); // 单聊的接口有返回是否已读，
                                // 漫游的默认已上传
                                chatMessage.setUpload(true);
                                chatMessage.setUploadSchedule(100);
                                chatMessage.setMessageState(Constants.MESSAGE_SEND_SUCCESS);

                                if (TextUtils.isEmpty(chatMessage.getPacketId())) {
                                    if (!TextUtils.isEmpty(data.getMessageId())) {
                                        chatMessage.setPacketId(data.getMessageId());
                                    } else {
                                        chatMessage.setPacketId(ToolUtils.getUUID());
                                    }
                                }
                                if (ChatMessageDao.getInstance().roamingMessageFilter(chatMessage.getType())) {
                                    ChatMessageDao.getInstance().saveRoamingChatMessage(mLoginUserId, mFriend.getUserId(), chatMessage);
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    private void initView() {
        mChatMessages = new ArrayList<>();
        mChatBottomView = (ChatBottomView) findViewById(R.id.chat_bottom_view);
        mChatBottomView.setBqKeyBoardListener(new ChatFaceView.BqMustInfoListener() {
            @Override
            public FragmentManager getFragmentManager() {
                return getSupportFragmentManager();
            }

            @Override
            public ChatFaceView.BqKeyBoardListener getBqKeyBoardListener() {
                return bqKeyBoardListener;
            }

            @Override
            public CoreManager getCoreManager() {
                return coreManager;
            }

            @Override
            public ChatFaceView.CustomBqListener getCustomBqListener() {
                return customBqListener;
            }

            @Override
            public ChatFaceView.MyBqListener getMyBqListener() {
                return myBqListener;
            }
        });
        mChatContentView = (MucChatContentView) findViewById(R.id.chat_content_view);
        refresh = (SmartRefreshLayout) findViewById(R.id.refresh);
        refresh.setRefreshHeader(new ClassicsHeader(this).setEnableLastTime(false));
//        mChatContentView.setOnMenuClickListener(view -> moreSelected(false, 0));
        mChatContentView.setItemAnimator(null);
        mChatBottomView.setChatBottomListener(this);
        mChatBottomView.getmShotsLl().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatBottomView.getmShotsLl().setVisibility(View.GONE);
                String shots = PreferenceUtils.getString(mContext, Constants.SCREEN_SHOTS, "No_Shots");
                QuickSendPreviewActivity.startForResult(ChatActivity.this, shots, REQUEST_CODE_QUICK_SEND);
            }
        });
        if (mFriend.getIsDevice() == 1) {
            mChatBottomView.setEquipment(true);
            mChatContentView.setChatListType(MucChatContentView.ChatListType.DEVICE);
        }
        mChatContentView.setToUserId(mFriend.getUserId());
        mChatContentView.setData(mChatMessages);
        mChatContentView.setChatBottomView(mChatBottomView);// 需要获取多选菜单的点击事件
        mChatContentView.setMessageEventListener(this);
        refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                loadDatas(false);
            }
        });
        // 有阅后即焚消息显示时禁止截屏，
        mChatContentView.setRecyclerViewOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean needSecure = false;

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                RecyclerView.LayoutManager mLinearLayoutManager = ((RecyclerView) mChatContentView).getLayoutManager();
                LinearLayoutManager linearManager = (LinearLayoutManager) mLinearLayoutManager;
                int visibleItemCount = linearManager.getChildCount();
                int totalItemCount = linearManager.getItemCount();
                int firstVisibleItemPositiont = linearManager.findFirstVisibleItemPosition();
                int lastVisibleItemPosition = linearManager.findLastVisibleItemPosition();
                if (firstVisibleItemPositiont < 0 || visibleItemCount <= 0) {
                    return;
                }
                List<ChatMessage> visibleList = mChatMessages.subList(firstVisibleItemPositiont, Math.min(lastVisibleItemPosition + visibleItemCount, totalItemCount));
                boolean lastSecure = needSecure;
                needSecure = false;
                for (ChatMessage message : visibleList) {
                    if (message.getIsReadDel()) {
                        needSecure = true;
                        break;
                    }
                }
                if (needSecure != lastSecure) {
                    if (needSecure) {
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
                    } else {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                    }
                }

            }
        });

        CoreManager.updateMyBalance();
        if (isNotificationComing) {
            Intent intent = new Intent();
            intent.putExtra(AppConstant.EXTRA_FRIEND, mFriend);
            intent.setAction(Constants.NOTIFY_MSG_SUBSCRIPT);
            sendBroadcast(intent);
        } else {
            FriendDao.getInstance().markUserMessageRead(mLoginUserId, mFriend.getUserId());
        }
        loadDatas(true);
        if (mFriend.getDownloadTime() < mFriend.getTimeSend()) {// 自动同步其他端的聊天记录
            synchronizeChatHistory();
        }
    }

    private void loadDatas(boolean scrollToBottom) {
        if (mChatMessages.size() > 0) {
            mMinId = mChatMessages.get(0).getTimeSend();
        } else {
            ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(mLoginUserId, mFriend.getUserId());
            if (chat != null && chat.getTimeSend() != 0) {
                mMinId = chat.getTimeSend() + 2;
            } else {
                mMinId = TimeUtils.time_current_time();
            }
        }
        List<ChatMessage> chatLists;
        if (isSearch) {// 查询时就不做分页限制了，因为被查询的消息可能在二十条以外
            chatLists = ChatMessageDao.getInstance().searchMessagesByTime(mLoginUserId,
                    mFriend.getUserId(), mSearchTime);
        } else {
            chatLists = ChatMessageDao.getInstance().getSingleChatMessages(mLoginUserId,
                    mFriend.getUserId(), mMinId, mPageSize);
        }

        if (chatLists == null || chatLists.size() <= 0) {
            if (!scrollToBottom) {// 加载漫游
                getNetSingle();
            }
        } else {
            if (mTvTitle != null) {
                mTvTitle.post(new Runnable() {
                    @Override
                    public void run() {
                        long currTime = TimeUtils.time_current_time();
                        List<ChatMessage> mDatas = new ArrayList<>();
                        mDatas.clear();
                        for (int i = 0; i < chatLists.size(); i++) {
                            ChatMessage message = chatLists.get(i);
                            // 防止过期的消息出现在列表中
                            if (message.getIsReadDelByInt() > 0 && message.getDeleteTime() > 0 && message.getDeleteTime() < currTime) {
                                ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), message.getPacketId());
                                continue;
                            }
                            // 为防止 下拉 加载时 因为数据库时间精度问题 导致重复数据被查出。
                            if (packetIdMap.get(message.getPacketId()) != null) {
                                continue;
                            } else {
                                packetIdMap.put(message.getPacketId(), mFriend.getUserId());
                            }
                            mDatas.add(0, message);
                        }
                        mChatMessages.addAll(0, mDatas);
                        if (isSearch) {
                            isSearch = false;
                            int position = 0;
                            for (int i = 0; i < mChatMessages.size(); i++) {
                                if (mChatMessages.get(i).getDoubleTimeSend() == mSearchTime) {
                                    position = i;
                                }
                            }
                            mChatContentView.smoothMoveToPosition(mChatContentView, position, false);// 定位到该条消息
                        } else {
                            if (scrollToBottom) {
                                mChatContentView.notifyDataSetInvalidated(scrollToBottom);
                            } else {
                                mChatContentView.notifyItemInserted(0, mDatas.size());
                            }
                        }
                        refresh.finishRefresh();
                        if (!mHasMoreData) {
                            refresh.setEnableRefresh(false);
                        }
                    }
                });
            }
        }
    }

    protected void onSaveContent() {
        String str = mChatBottomView.getmChatEdit().getText().toString().trim();
        // 清除 回车与空格
        str = str.replaceAll("\\s", "");
        str = str.replaceAll("\\n", "");
        if (TextUtils.isEmpty(str)) {
            if (XfileUtils.isNotEmpty(mChatMessages)) {
                ChatMessage chat = mChatMessages.get(mChatMessages.size() - 1);
                if (chat.getType() == Constants.TYPE_TEXT && chat.getIsReadDel()) {
                    FriendDao.getInstance().updateFriendContent(
                            mLoginUserId,
                            mFriend.getUserId(),
                            getString(R.string.tip_click_to_read),
                            chat.getType(),
                            chat.getTimeSend());
                } else {
                    FriendDao.getInstance().updateFriendContent(
                            mLoginUserId,
                            mFriend.getUserId(),
                            chat.getContent(),
                            chat.getType(),
                            chat.getTimeSend());
                }
            }
        } else {// [草稿]
            FriendDao.getInstance().updateFriendContent(
                    mLoginUserId,
                    mFriend.getUserId(),
                    "&8824" + str,
                    Constants.TYPE_TEXT, TimeUtils.time_current_time());
        }
        PreferenceUtils.putString(mContext, "WAIT_SEND" + mFriend.getUserId() + mLoginUserId, str);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 忽略双指操作，避免引起莫名的问题，
        if (ev.getActionIndex() > 0) {
            return true;
        }
        try {
            return super.dispatchTouchEvent(ev);
        } catch (IllegalArgumentException ignore) {
            // 可能触发ViewPager的bug, 找不到手指头，
            // https://stackoverflow.com/a/31306753
            return true;
        }
    }

    private void doBack() {
        if (!TextUtils.isEmpty(instantMessage)) {
            SelectionFrame selectionFrame = new SelectionFrame(this);
            selectionFrame.setSomething(null, getString(R.string.tip_forwarding_quit), new SelectionFrame.OnSelectionFrameClickListener() {
                @Override
                public void cancelClick() {

                }

                @Override
                public void confirmClick() {
                    onSaveContent();
                    MsgBroadcast.broadcastMsgUiUpdate(mContext);
                    finish();
                }
            });
            selectionFrame.show();
        } else {
            onSaveContent();
            MsgBroadcast.broadcastMsgUiUpdate(mContext);
            finish();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (!JVCideoPlayerStandardforchat.handlerBack()) {
            doBack();
        }
    }

    @Override
    protected boolean onHomeAsUp() {
        doBack();
        return true;
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        mBlackList = FriendDao.getInstance().getAllBlacklists(mLoginUserId);
        instantChatMessage();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Glide.with(this).resumeRequests();
        if (mFriend != null) {
            // 获取[草稿]
            String draft = PreferenceUtils.getString(mContext, "WAIT_SEND" + mFriend.getUserId() + mLoginUserId, "");
            if (!TextUtils.isEmpty(draft)) {
                String s = StringUtils.replaceSpecialChar(draft);
                CharSequence content = HtmlUtils.transform200SpanString(s.replaceAll("\n", "\r\n"), true);
                mChatBottomView.getmChatEdit().setText(content);
                softKeyboardControl(true);
            }
            // 获取阅后即焚状态(因为用户可能到聊天设置界面 开启/关闭 阅后即焚，所以在onResume时需要重新获取下状态)
            int currentReadDel = PreferenceUtils.getInt(mContext, Constants.MESSAGE_READ_FIRE + mFriend.getUserId() + mLoginUserId, 0);
            //开关阅后即焚  更改 右上角 的 图标 如果 当前的值 和以前的值不一样 就 就添加一条更改阅后即焚的消息
            boolean isChange = currentReadDel != isReadDel;
            if (isChange) {
                //如果自己改变了阅后即焚
                ChatMessage addChangeReadDelTimeView = createChangeReadDelTimeView(currentReadDel, true);
                if (addChangeReadDelTimeView != null) {
                    mChatMessages.add(addChangeReadDelTimeView);
                }
                mChatContentView.notifyItemInserted(mChatMessages.size() - 1);
            }
            changeActionBarRightStatus(currentReadDel);
            isReadDel = currentReadDel;

            // 记录当前聊天对象的id
            MyApplication.IsRingId = mFriend.getUserId();
        }
    }


    /**
     * 改变右上角 图标状态
     *
     * @param isReadDel 改变的新的值
     */
    private void changeActionBarRightStatus(int isReadDel) {
        if (isReadDel == 0) {
            findViewById(R.id.iv_title_right).setVisibility(View.VISIBLE);
            View view = findViewById(R.id.read_del_ll_view);
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        } else {
            View view = findViewById(R.id.iv_title_right);
            view.setVisibility(View.GONE);
            if (findViewById(R.id.read_del_ll_view) != null) {
                TextView tv = findViewById(R.id.read_del_tv_view);
                tv.setText(PersonSettingActivity.handlerMsg(isReadDel));
                findViewById(R.id.read_del_ll_view).setVisibility(View.VISIBLE);
            } else {
                RelativeLayout actionRoot = findViewById(R.id.rl_content);
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setId(R.id.read_del_ll_view);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setGravity(Gravity.CENTER);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(DisplayUtil.dip2px(this, 46), DisplayUtil.dip2px(this, 46));
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                layoutParams.setMargins(0, 0, DisplayUtil.dip2px(this, 10), 0);
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                linearLayout.setLayoutParams(layoutParams);
                ImageView icon = new ImageView(this);
                icon.setImageResource(R.mipmap.blue_alarm_clock);
                LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(DisplayUtil.dip2px(this, 20), DisplayUtil.dip2px(this, 20));
                icon.setLayoutParams(iconLp);
                linearLayout.addView(icon);
                TextView textView = new TextView(this);
                textView.setId(R.id.read_del_tv_view);
                textView.setText(PersonSettingActivity.handlerMsg(isReadDel));
                textView.setTextSize(8);
                textView.setTextColor(getResources().getColor(R.color.color_0093FF));
                LinearLayout.LayoutParams tvlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tvlp.setMargins(0, DisplayUtil.dip2px(this, 3), 0, 0);
                textView.setLayoutParams(tvlp);
                linearLayout.addView(textView);
                if (actionRoot != null) {
                    actionRoot.addView(linearLayout);
                }
                findViewById(R.id.read_del_ll_view).setVisibility(View.VISIBLE);
                findViewById(R.id.read_del_ll_view).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPersonSetting();
                    }
                });
            }

        }

    }

    private void startPersonSetting() {
        mChatBottomView.reset();
        mChatBottomView.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ChatActivity.this, PersonSettingActivity.class);
                intent.putExtra("ChatObjectId", mFriend.getUserId());
                startActivity(intent);
            }
        }, 100);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Glide.with(this).pauseRequests();
        if (TextUtils.isEmpty(mChatBottomView.getmChatEdit().getText().toString())) {// 清空草稿，以防消息发送出去后，通过onPause--onResume的方式给输入框赋值
            PreferenceUtils.putString(mContext, "WAIT_SEND" + mFriend.getUserId() + mLoginUserId, "");
        }
        // 将当前聊天对象id重置
        MyApplication.IsRingId = "Empty";

        VoicePlayer.instance().stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JCVideoPlayer.releaseAllVideos();
        mChatBottomView.recordCancel();
        ListenerManager.getInstance().removeChatMessageListener(this);
        EventBus.getDefault().unregister(this);
        unregisterReceiver(receiver);
        ReadDelManager.getInstants().release();
    }

    /***************************************
     * ChatContentView的回调
     ***************************************/
    @Override
    public void onMyAvatarClick() {
        // 自己的头像点击
        mChatBottomView.reset();
        mChatBottomView.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mContext, BasicInfoActivity.class);
                intent.putExtra(AppConstant.EXTRA_USER_ID, mLoginUserId);
                startActivity(intent);
            }
        }, 100);
    }

    @Override
    public void onFriendAvatarClick(final String friendUserId) {
        // 朋友的头像点击
        mChatBottomView.reset();
        mChatBottomView.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mContext, BasicInfoActivity.class);
                intent.putExtra(AppConstant.EXTRA_USER_ID, friendUserId);
                startActivity(intent);
            }
        }, 100);
    }

    @Override
    public void LongAvatarClick(ChatMessage chatMessage) {
    }

    @Override
    public void onNickNameClick(String friendUserId) {
    }

    @Override
    public void onMessageClick(ChatMessage chatMessage) {
    }

    @Override
    public void onMessageLongClick(ChatMessage chatMessage) {
    }

    @Override
    public void onEmptyTouch() {
        mChatBottomView.reset();
    }

    @Override
    public void onTipMessageClick(ChatMessage message) {
        if (message.getFileSize() == Constants.TYPE_83) {
            showRedReceivedDetail(message.getFilePath());
        } else if (message.getFileSize() == Constants.TYPE_CLOUD_RECEIVE_RED) {
            showCloudRedReceivedDetail(message.getFilePath());
        }
    }

    @Override
    public void onChangeReadDelTimeClick(View v, AChatHolderInterface holder, ChatMessage message) {
        Intent intent = new Intent(mContext, PersonSettingActivity.class);
        intent.putExtra("ChatObjectId", mFriend.getUserId());
        mContext.startActivity(intent);
    }

    // 查看红包领取详情
    private void showRedReceivedDetail(String redId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(mContext).accessToken);
        params.put("id", redId);

        HttpUtils.get().url(CoreManager.requireConfig(mContext).RENDPACKET_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<OpenRedpacket>(OpenRedpacket.class) {

                    @Override
                    public void onResponse(ObjectResult<OpenRedpacket> result) {
                        if (result.getData() != null) {
                            // 当resultCode==1时，表示可领取
                            // 当resultCode==0时，表示红包已过期、红包已退回、红包已领完
                            OpenRedpacket openRedpacket = result.getData();
                            Bundle bundle = new Bundle();
                            Intent intent = new Intent(mContext, RedDetailsActivity.class);
                            bundle.putSerializable("openRedpacket", openRedpacket);
                            bundle.putInt("redAction", 0);
                            if (!TextUtils.isEmpty(result.getResultMsg())) //resultMsg不为空表示红包已过期
                            {
                                bundle.putInt("timeOut", 1);
                            } else {
                                bundle.putInt("timeOut", 0);
                            }

                            bundle.putBoolean("isGroup", false);
                            bundle.putString("mToUserId", mFriend.getUserId());
                            intent.putExtras(bundle);
                            mContext.startActivity(intent);
                        } else {
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showToast(mContext, getString(R.string.data_exception));
                    }
                });
    }

    // 查看云红包领取详情
    private void showCloudRedReceivedDetail(String redId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(mContext).accessToken);
        params.put("requestId", redId);

        HttpUtils.post().url(CoreManager.requireConfig(mContext).REDPACKET_CREATE_INQUIRE)
                .params(params)
                .build()
                .execute(new BaseCallback<CloudQueryRedPacket>(CloudQueryRedPacket.class) {

                    @Override
                    public void onResponse(ObjectResult<CloudQueryRedPacket> result) {
                        if (result.getData() != null) {
                            Intent intent = new Intent(mContext, CloudRedDetailsActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("requestId", result.getData().getRequestId());
                            bundle.putBoolean("isGroup", false);
                            bundle.putString("mToUserId", mFriend.getUserId());
                            intent.putExtras(bundle);
                            mContext.startActivity(intent);
                        } else {
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showToast(mContext, getString(R.string.data_exception));
                    }
                });
    }

    @Override
    public void onReplayClick(ChatMessage message) {
        ChatMessage replayMessage = new ChatMessage(message.getObjectId());
        AsyncUtils.doAsync(this, t -> {
            Reporter.post("查询被回复的消息出错<" + message.getObjectId() + ">", t);
        }, c -> {
            List<ChatMessage> chatMessages = ChatMessageDao.getInstance().searchFromMessage(c.getRef(), mLoginUserId, mFriend.getUserId(), replayMessage);
            if (chatMessages == null) {
                // 没查到消息，
                Log.e("Replay", "本地没有查到被回复的消息<" + message.getObjectId() + ">");
                return;
            }
            int index = -1;
            for (int i = 0; i < chatMessages.size(); i++) {
                ChatMessage m = chatMessages.get(i);
                if (TextUtils.equals(m.getPacketId(), replayMessage.getPacketId())) {
                    index = i;
                }
            }
            if (index == -1) {
                Reporter.unreachable();
                return;
            }
            int finalIndex = index;
            c.uiThread(r -> {
                mChatMessages = chatMessages;
                mChatContentView.setData(mChatMessages);
                mChatContentView.notifyDataSetInvalidated(finalIndex);
            });
        });
    }

    /**
     * 点击感叹号重新发送
     */
    @Override
    public void onSendAgain(ChatMessage message) {
        ImHelper.reSendFailSendChatMessage(message, mFriend, coreManager, mUploadResponse);
    }

    public void deleteMessage(String msgIdListStr) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", msgIdListStr);
        params.put("delete", "1");  // 1单方删除 2-双方删除
        params.put("type", "1");    // 1单聊记录 2-群聊记录

        HttpUtils.get().url(coreManager.getConfig().USER_DEL_CHATMESSAGE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    /**
     * 消息撤回
     */
    @Override
    public void onMessageBack(final ChatMessage chatMessage, final int position) {
        DialogHelper.showMessageProgressDialog(this, InternationalizationHelper.getString("MESSAGE_REVOCATION"));
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", chatMessage.getPacketId());
        params.put("delete", "2");  // 1单方删除 2-双方删除
        params.put("type", "1");    // 1单聊记录 2-群聊记录

        HttpUtils.get().url(coreManager.getConfig().USER_DEL_CHATMESSAGE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (chatMessage.getType() == Constants.TYPE_VOICE) {// 撤回的为语音消息，停止播放
                            if (VoicePlayer.instance().getVoiceMsgId().equals(chatMessage.getPacketId())) {
                                VoicePlayer.instance().stop();
                            }
                        } else if (chatMessage.getType() == Constants.TYPE_VIDEO) {
                            JCVideoPlayer.releaseAllVideos();
                        }
                        // 发送撤回消息
                        ChatMessage message = new ChatMessage();
                        message.setType(Constants.TYPE_BACK);
                        message.setFromUserId(mLoginUserId);
                        message.setFromUserName(coreManager.getSelf().getNickName());
                        message.setToUserId(mFriend.getUserId());
                        message.setContent(chatMessage.getPacketId());
                        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
                        message.setPacketId(ToolUtils.getUUID());
                        ImHelper.sendChatMessage(mFriend.getUserId(), message);
                        ChatMessage chat = mChatMessages.get(position);
                        ChatMessageDao.getInstance().updateMessageBack(mLoginUserId, mFriend.getUserId(), chat.getPacketId(), getString(R.string.you));
                        chat.setType(Constants.TYPE_TIP);
                        //  chat.setContent("你撤回了一条消息");
                        chat.setContent(InternationalizationHelper.getString("JX_AlreadyWithdraw"));
                        mChatContentView.notifyItemChanged(position);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    @Override
    public void onMessageReplay(ChatMessage chatMessage) {
        replayMessage = chatMessage;
        mChatBottomView.setReplay(chatMessage);
    }

    @Override
    public void cancelReplay() {
        replayMessage = null;
    }


    @Override
    public void onCallListener(int type) {
        if (ImHelper.checkXmppAuthenticated()) {
            if (type == 103 || type == 104) {
                Log.e(TAG, "dialAudioCall");
                dial(1);
            } else if (type == 113 || type == 114) {
                Log.e(TAG, "dialVideoCall");
                dial(2);
            }
        }
    }

    private void dial(final int type) {
        if (MyApplication.IS_OPEN_CLUSTER) {// 集群，调接口获取 meetUrl
            Log.i("info","shipin===isopen==");
            Map<String, String> params = new HashMap<>();
            params.put("access_token", coreManager.getSelfStatus().accessToken);
            String area = PreferenceUtils.getString(this, AppConstant.EXTRA_CLUSTER_AREA);
            if (!TextUtils.isEmpty(area)) {
                params.put("area", area);
            }
            params.put("toUserId", mFriend.getUserId());

            HttpUtils.get().url(coreManager.getConfig().OPEN_MEET)
                    .params(params)
                    .build()
                    .execute(new BaseCallback<String>(String.class) {
                        @Override
                        public void onResponse(ObjectResult<String> result) {
                            if (!TextUtils.isEmpty(result.getData())) {
                                Log.i("info","lalalla===1");
                                JSONObject jsonObject = JSONObject.parseObject(result.getData());
                                realDial(type, jsonObject.getString("meetUrl"));
                            } else {
                                Log.i("info","lalalla===2");

                                realDial(type, null);
                            }
                        }

                        @Override
                        public void onError(Call call, Exception e) {// 获取网络配置失败，使用默认配置
                            realDial(type, null);
                        }
                    });
        } else {
            Log.i("info","shipin===isopen==2");

            realDial(type, null);
        }
    }

    private void realDial(int type, String meetUrl) {
        ChatMessage message = new ChatMessage();
        if (type == 1) {// 语音通话
            message.setType(Constants.TYPE_IS_CONNECT_VOICE);
            message.setContent(InternationalizationHelper.getString("JXSip_invite") + " " + InternationalizationHelper.getString("JX_VoiceChat"));
        } else {// 视频通话
            message.setType(Constants.TYPE_IS_CONNECT_VIDEO);
            message.setContent(InternationalizationHelper.getString("JXSip_invite") + " " + InternationalizationHelper.getString("JX_VideoChat"));
        }
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setToUserId(mFriend.getUserId());
        if (!TextUtils.isEmpty(meetUrl)) {
            message.setFilePath(meetUrl);
        }
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(coreManager.getSelf().getNickName());
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        message.setPacketId(ToolUtils.getUUID());
        ImHelper.sendChatMessage(mFriend.getUserId(), message);
        Intent intent = new Intent(this, Jitsi_pre.class);
        if (type == 1) {
            intent.putExtra("isvoice", true);
        } else {
            Log.i("info","shipin====dial==");
            intent.putExtra("isvoice", false);
        }
        intent.putExtra("fromuserid", mLoginUserId);
        intent.putExtra("touserid", mFriend.getUserId());
        intent.putExtra("username", mFriend.getNickName());
        if (!TextUtils.isEmpty(meetUrl)) {
            intent.putExtra("meetUrl", meetUrl);
        }
        startActivity(intent);
    }

    /***************************************
     * ChatBottomView的回调
     ***************************************/

    private void softKeyboardControl(boolean isShow) {
        // 软键盘消失
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm == null) return;
        if (isShow) {
            mChatBottomView.postDelayed(new Runnable() {
                @Override
                public void run() {// 延迟200ms在弹起，否则容易出现页面未完全加载完成软键盘弹起的效果
                    mChatBottomView.getmChatEdit().requestFocus();
                    mChatBottomView.getmChatEdit().setSelection(mChatBottomView.getmChatEdit().getText().toString().length());
                    imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                }
            }, 200);
        } else {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 发送一条包装好的消息
     */
    private void sendMessage(final ChatMessage message) {
        mChatMessages.add(message);
        mChatContentView.notifyItemInserted(mChatMessages.size() - 1);
        PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(this);
        boolean isSupport = privacySetting.getMultipleDevices() == 1;
        if (isSupport) {
            message.setFromId(MyApplication.MULTI_RESOURCE);
        } else {
            message.setFromId("youjob");
        }
        if (mFriend.getIsDevice() == 1) {
            message.setToUserId(userId);
            message.setToId(mFriend.getUserId());
        } else {
            message.setToUserId(mFriend.getUserId());

            // sz 消息过期时间
//            if (mFriend.getChatRecordTimeOut() == -1 || mFriend.getChatRecordTimeOut() == 0) {// 永久
            message.setDeleteTime(-1);   //没有消息过期时间了  所以发出的消息都是 -1
//            } else {
//                long deleteTime = TimeUtils.time_current_time() + (long) (mFriend.getChatRecordTimeOut() * 24 * 60 * 60);
//                message.setDeleteTime(deleteTime);
//            }
        }
        boolean isEncrypt = privacySetting.getIsEncrypt() == 1;
        if (isEncrypt) {
            message.setIsEncrypt(1);
        } else {
            message.setIsEncrypt(0);
        }
        message.setReSendCount(ChatMessageDao.fillReCount(message.getType()));
        // 将消息保存在数据库了
        ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mFriend.getUserId(), message);
        //检测发送的这条消息是否是 阅后即焚

        if (message.getType() == Constants.TYPE_VOICE || message.getType() == Constants.TYPE_IMAGE
                || message.getType() == Constants.TYPE_VIDEO || message.getType() == Constants.TYPE_FILE
                || message.getType() == Constants.TYPE_LOCATION) {// 语音、图片、视频、文件需要上传在发送
            // 位置消息也要上传截图，
            if (!message.isUpload()) {// 未上传
                if (mFriend.getIsDevice() == 1) {
                    // 我的设备会出问题
                    // UploadEngine.uploadImFile(coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), userId, message, mUploadResponse);
                    UploadEngine.uploadImFile(ChatActivity.this, coreManager, coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), mFriend.getUserId(), message, mUploadResponse);
                } else {
                    UploadEngine.uploadImFile(ChatActivity.this, coreManager, coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), mFriend.getUserId(), message, mUploadResponse);
                }
            } else {// 已上传 自定义表情默认为已上传
                sendMsg(message);
            }
        } else {// 其他类型直接发送
            sendMsg(message);
        }
    }

    private void sendMsg(ChatMessage message) {
        // 一些异步回调进来的也要判断xmpp是否在线，
        // 比如图片上传成功后，
        if (!ImHelper.checkXmppAuthenticated() && !HttpUtil.isGprsOrWifiConnected(this)) {
            ToastUtil.showToast(this, getString(R.string.tip_network_error));
            return;
        }
        if (mFriend.getIsDevice() == 1) {
            ImHelper.sendChatMessage(userId, message);
        } else {
            ImHelper.sendChatMessage(mFriend.getUserId(), message);
        }
    }

    /**
     * 停止播放聊天的录音
     */
    @Override
    public void stopVoicePlay() {
        VoicePlayer.instance().stop();
    }

    @Override
    public void sendAt() {
    }

    @Override
    public void sendAtMessage(String text) {
        sendText(text);// 单聊内包含@符号的消息也需要发出去
    }

    @Override
    public void sendText(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (!ImHelper.checkXmppAuthenticated() && !HttpUtil.isGprsOrWifiConnected(this)) {
            ToastUtil.showToast(this, getString(R.string.tip_network_error));
            return;
        }
        if (isBlackFriend()) {// 该用户在你的黑名单列表内
            return;
        }
        ChatMessage message = new ChatMessage();
        // 文本类型
        message.setType(Constants.TYPE_TEXT);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        message.setPacketId(ToolUtils.getUUID());
        message.setContent(text);
        if (replayMessage != null) {
            message.setType(Constants.TYPE_REPLAY);
            message.setObjectId(replayMessage.toJsonString());
            replayMessage = null;
            mChatBottomView.resetReplay();
        }
        message.setIsReadDel(isReadDel);
       /* mChatContentView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mChatContentView.scrollToBottom();
            }
        }, 500);*/
        sendMessage(message);
        // 遍历消息集合，查询红包类型消息
        for (int i = mChatMessages.size() - 1; i >= 0; i--) {
            ChatMessage msg = mChatMessages.get(i);
            if (msg.getType() == Constants.TYPE_RED// 红包
                    && StringUtils.strEquals(msg.getFilePath(), "3")// 口令红包
                    && text.equalsIgnoreCase(msg.getContent())// 发送的文本与口令一致
                    && msg.getFileSize() == 1// 可以领取的状态
                    && !msg.isMySend()) {
                RedDialogBean redDialogBean = new RedDialogBean(msg.getFromUserId(), msg.getFromUserName(),
                        msg.getContent(), null, 3, CoreManager.getSelf(mContext).getRedPacketVip(), false);
                int finalI = i;
                mRedDialog = new RedDialog(mContext, redDialogBean, new RedDialog.OnClickRedListener() {
                    @Override
                    public void clickRed() {
                        // 打开红包
                        openRedPacket(msg, finalI);
                    }

                    @Override
                    public void clickPrivilege(String money) {

                    }
                });
                softKeyboardControl(false);
                mRedDialog.show();
                break;
            }
        }
    }

    /**
     * 打开红包
     */
    public void openRedPacket(final ChatMessage message, int postion) {
        String redId = message.getObjectId();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("id", redId);

        HttpUtils.get().url(coreManager.getConfig().REDPACKET_OPEN)
                .params(params)
                .build()
                .execute(new BaseCallback<OpenRedpacket>(OpenRedpacket.class) {

                    @Override
                    public void onResponse(ObjectResult<OpenRedpacket> result) {
                        if (mRedDialog != null) {
                            mRedDialog.dismiss();
                        }
                        if (result.getData() != null) {
                            // 标记已经领取过了一次,不可再领取
                            message.setFileSize(2);
                            ChatMessageDao.getInstance().updateChatMessageReceiptStatus(mLoginUserId, mFriend.getUserId(), message.getPacketId());
                            mChatContentView.notifyItemChanged(postion);

                            OpenRedpacket openRedpacket = result.getData();
                            Bundle bundle = new Bundle();
                            Intent intent = new Intent(mContext, RedDetailsActivity.class);
                            bundle.putSerializable("openRedpacket", openRedpacket);
                            bundle.putInt("redAction", 1);
                            bundle.putInt("timeOut", 0);

                            bundle.putBoolean("isGroup", false);
                            bundle.putString("mToUserId", mFriend.getUserId());
                            intent.putExtras(bundle);
                            mContext.startActivity(intent);
                            // 更新余额
                            coreManager.updateMyBalance();
                            showReceiverRedLocal(openRedpacket);
                        } else {
                            ToastUtil.showToast(ChatActivity.this, result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (mRedDialog != null) {
                            mRedDialog.dismiss();
                        }
                    }
                });
    }

    private void showReceiverRedLocal(OpenRedpacket openRedpacket) {
        // 本地显示一条领取通知
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setFileSize(Constants.TYPE_83);
        chatMessage.setFilePath(openRedpacket.getPacket().getId());
        chatMessage.setFromUserId(mLoginUserId);
        chatMessage.setFromUserName(mLoginNickName);
        chatMessage.setToUserId(mFriend.getUserId());
        chatMessage.setType(Constants.TYPE_TIP);
        chatMessage.setContent(getString(R.string.red_received_self, openRedpacket.getPacket().getUserName()));
        chatMessage.setPacketId(ToolUtils.getUUID());
        chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mFriend.getUserId(), chatMessage)) {
            mChatMessages.add(chatMessage);
            mChatContentView.notifyItemInserted(mChatMessages.size() - 1);
        }
    }

    private void showReceiverCloudRedLocal(CloudQueryRedPacket receiversBean) {
        // 本地显示一条领取通知
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setFileSize(Constants.TYPE_CLOUD_RECEIVE_RED);
        chatMessage.setFilePath(receiversBean.getRequestId());
        chatMessage.setFromUserId(mLoginUserId);
        chatMessage.setFromUserName(mLoginNickName);
        chatMessage.setToUserId(mFriend.getUserId());
        chatMessage.setType(Constants.TYPE_TIP);
        chatMessage.setContent(getString(R.string.cloud_red_received_self, receiversBean.getNickName()));
        chatMessage.setPacketId(ToolUtils.getUUID());
        chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mFriend.getUserId(), chatMessage)) {
            mChatMessages.add(chatMessage);
            mChatContentView.notifyItemInserted(mChatMessages.size() - 1);
        }
    }

    @Override
    public void sendGif(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (!ImHelper.checkXmppAuthenticated() && !HttpUtil.isGprsOrWifiConnected(this)) {
            ToastUtil.showToast(this, getString(R.string.tip_network_error));
            return;
        }
        if (isBlackFriend()) {// 该用户在你的黑名单列表内
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(Constants.TYPE_GIF);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        message.setPacketId(ToolUtils.getUUID());
        message.setContent(text);
        message.setIsReadDel(isReadDel);
        sendMessage(message);
    }

    @Override
    public void sendCollection(String collection) {
        if (!ImHelper.checkXmppAuthenticated() && !HttpUtil.isGprsOrWifiConnected(this)) {
            ToastUtil.showToast(this, getString(R.string.tip_network_error));
            return;
        }
        if (isBlackFriend()) {// 该用户在你的黑名单列表内
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(Constants.TYPE_IMAGE);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(collection);
        message.setUpload(true);// 自定义表情，不需要上传
        message.setIsReadDel(isReadDel);
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        message.setPacketId(ToolUtils.getUUID());
        sendMessage(message);
    }

    @Override
    public void sendVoice(String filePath, int timeLen) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        if (!ImHelper.checkXmppAuthenticated() && !HttpUtil.isGprsOrWifiConnected(this)) {
            ToastUtil.showToast(this, getString(R.string.tip_network_error));
            return;
        }
        if (isBlackFriend()) {// 该用户在你的黑名单列表内
            return;
        }
        File file = new File(filePath);
        long fileSize = file.length();
        ChatMessage message = new ChatMessage();
        message.setType(Constants.TYPE_VOICE);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent("");
        message.setFilePath(filePath);
        message.setFileSize((int) fileSize);
        message.setTimeLen(timeLen);
        message.setIsReadDel(isReadDel);
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        message.setPacketId(ToolUtils.getUUID());
        sendMessage(message);
    }

    public void sendImage(File file) {
        if (!file.exists()) {
            return;
        }
        if (!ImHelper.checkXmppAuthenticated() && !HttpUtil.isGprsOrWifiConnected(this)) {
            ToastUtil.showToast(this, getString(R.string.tip_network_error));
            return;
        }
        if (isBlackFriend()) {// 该用户在你的黑名单列表内
            return;
        }
        long fileSize = file.length();
        ChatMessage message = new ChatMessage();
        message.setType(Constants.TYPE_IMAGE);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent("");
        String filePath = file.getAbsolutePath();
        message.setFilePath(filePath);
        message.setFileSize((int) fileSize);
        int[] imageParam = ImageUtils.getImageWidthHeight(filePath);
        message.setLocation_x(String.valueOf(imageParam[0]));
        message.setLocation_y(String.valueOf(imageParam[1]));
        message.setIsReadDel(isReadDel);
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        message.setPacketId(ToolUtils.getUUID());
        sendMessage(message);
    }

    public void sendVideo(File file) {
        if (!file.exists()) {
            return;
        }
        if (!ImHelper.checkXmppAuthenticated() && !HttpUtil.isGprsOrWifiConnected(this)) {
            ToastUtil.showToast(this, getString(R.string.tip_network_error));
            return;
        }
        if (isBlackFriend()) {// 该用户在你的黑名单列表内
            return;
        }
        long fileSize = file.length();
        ChatMessage message = new ChatMessage();
        message.setType(Constants.TYPE_VIDEO);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent("");
        String filePath = file.getAbsolutePath();
        message.setFilePath(filePath);
        message.setFileSize((int) fileSize);
        message.setIsReadDel(isReadDel);
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        message.setPacketId(ToolUtils.getUUID());
        sendMessage(message);
    }

    public void sendFile(File file) {
        if (!file.exists()) {
            return;
        }
        if (!ImHelper.checkXmppAuthenticated() && !HttpUtil.isGprsOrWifiConnected(this)) {
            ToastUtil.showToast(this, getString(R.string.tip_network_error));
            return;
        }
        if (isBlackFriend()) {// 该用户在你的黑名单列表内
            return;
        }
        long fileSize = file.length();
        ChatMessage message = new ChatMessage();
        message.setType(Constants.TYPE_FILE);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent("");
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        String filePath = file.getAbsolutePath();
        message.setFilePath(filePath);
        message.setFileSize((int) fileSize);
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        message.setPacketId(ToolUtils.getUUID());
        sendMessage(message);
    }

    private void sendContacts(List<Contacts> contactsList) {
        for (Contacts contacts : contactsList) {
            sendText(contacts.getName() + '\n' + contacts.getTelephone());
        }
    }

    public void sendLocate(double latitude, double longitude, String address, String snapshot) {
        if (!ImHelper.checkXmppAuthenticated() && !HttpUtil.isGprsOrWifiConnected(this)) {
            ToastUtil.showToast(this, getString(R.string.tip_network_error));
            return;
        }
        if (isBlackFriend()) {// 该用户在你的黑名单列表内
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(Constants.TYPE_LOCATION);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        // 上传图片后会给赋值，
        message.setContent("");
        message.setFilePath(snapshot);
        message.setLocation_x(latitude + "");
        message.setLocation_y(longitude + "");
        message.setObjectId(address);
        message.setIsReadDel(isReadDel);
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        message.setPacketId(ToolUtils.getUUID());
        sendMessage(message);
    }

    @Override
    public void clickPhoto() {
        AndPermissionUtils.albumPermission(this, new OnPermissionClickListener() {
            @Override
            public void onSuccess() {
                // 将其置为true
                /*MyApplication.GalleyNotBackGround = true;
                CameraUtil.pickImageSimple(this, REQUEST_CODE_PICK_PHOTO);*/
                ArrayList<String> imagePaths = new ArrayList<>();
                PhotoPickerIntent intent = new PhotoPickerIntent(ChatActivity.this);
                intent.setSelectModel(SelectModel.MULTI);
                // 已选中的照片地址， 用于回显选中状态
                intent.setSelectedPaths(imagePaths);
                startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
                mChatBottomView.reset();
            }

            @Override
            public void onFailure(List<String> data) {
                ToastUtil.showToast(ChatActivity.this, getString(R.string.please_open_album_access));
            }
        });
    }

    @Override
    public void clickCamera() {
        AndPermissionUtils.shootVideo(this, new OnPermissionClickListener() {
            @Override
            public void onSuccess() {
                /* mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, CameraUtil.MEDIA_TYPE_IMAGE);
                CameraUtil.captureImage(this, mNewPhotoUri, REQUEST_CODE_CAPTURE_PHOTO);*/
                /* Intent intent = new Intent(this, EasyCameraActivity.class);
                startActivity(intent);*/
                mChatBottomView.reset();
                Intent intent = new Intent(ChatActivity.this, VideoRecorderActivity.class);
                startActivity(intent);
            }

            @Override
            public void onFailure(List<String> data) {
                ToastUtil.showToast(ChatActivity.this, getString(R.string.please_turn_on_camera_recording_permission));
            }
        });
    }

    @Override
    public void clickStartRecord() {
        // 现拍照录像ui和二为一，统一在clickCamera内处理
       /* Intent intent = new Intent(this, VideoRecorderActivity.class);
        startActivity(intent);*/
    }

    @Override
    public void clickLocalVideo() {
        // 现拍照录像ui和二为一，统一在clickCamera内处理
        Intent intent = new Intent(this, LocalVideoActivity.class);
        intent.putExtra(AppConstant.EXTRA_ACTION, AppConstant.ACTION_SELECT);
        intent.putExtra(AppConstant.EXTRA_MULTI_SELECT, true);
        startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
    }

    @Override
    public void clickAudio() {
        if (ImHelper.checkXmppAuthenticated()) {
            dial(1);
        }
    }

    @Override
    public void clickVideoChat() {
        if (ImHelper.checkXmppAuthenticated()) {
            Log.i("info","shipin====chat=Activity");
            dial(2);
        }
    }

    @Override
    public void clickFile() {
        SelectFileDialog dialog = new SelectFileDialog(this, new SelectFileDialog.OptionFileListener() {
            @Override
            public void option(List<File> files) {
                if (files != null && files.size() > 0) {
                    for (int i = 0; i < files.size(); i++) {
                        sendFile(files.get(i));
                    }
                }
            }

            @Override
            public void intent() {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
            }

        });
        dialog.show();
    }

    @Override
    public void clickContact() {
        SendContactsActivity.start(this, REQUEST_CODE_SEND_CONTACT);
    }

    @Override
    public void clickLocation() {
        Intent intent = new Intent(this, MapPickerActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SELECT_LOCATE);
    }

    @Override
    public void clickCard() {
        SelectCardPopupWindow mSelectCardPopupWindow = new SelectCardPopupWindow(this, this);
        mSelectCardPopupWindow.showAtLocation(findViewById(R.id.root_view),
                Gravity.CENTER, 0, 0);
    }

    @Override
    public void clickRedpacket() {
        Intent intent = new Intent(this, SendRedPacketActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SEND_RED);
    }

    /**
     * 云红包
     */
    @Override
    public void clickCloudRedEnvelope() {
        if (!ImHelper.checkXmppAuthenticated()) {
            return;
        }
        if (isBlackFriend()) {// 该用户在你的黑名单列表内
            return;
        }
        Intent intent = new Intent(this, SendCloudRedPacketActivity.class);
        intent.putExtra("userId", mFriend.getUserId());
        startActivityForResult(intent, REQUEST_CODE_SEND_RED_CLOUD);
    }

    @Override
    public void clickTransferMoney() {
        Intent intent = new Intent(this, TransferMoneyActivity.class);
        intent.putExtra(AppConstant.EXTRA_USER_ID, mFriend.getUserId());
        intent.putExtra(AppConstant.EXTRA_NICK_NAME, TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName());
        startActivity(intent);
    }

    /**
     * 微转账
     */
    @Override
    public void clickCloudTransfer() {
        Intent intent = new Intent(this, MicroTransferMoneyActivity.class);
        intent.putExtra(AppConstant.EXTRA_USER_ID, mFriend.getUserId());
        intent.putExtra(AppConstant.EXTRA_NICK_NAME, TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName());
        startActivity(intent);
    }

    @Override
    public void clickCollection() {
        Intent intent = new Intent(this, MyCollection.class);
        intent.putExtra("IS_SEND_COLLECTION", true);
        startActivityForResult(intent, REQUEST_CODE_SEND_COLLECTION);
    }

    @Override
    public void clickTwoWayWithdrawal() {
        //单聊咱不支持双向撤回
    }

    private void clickCollectionSend(int type, String content, int timeLen, String filePath, long fileSize) {
        if (!ImHelper.checkXmppAuthenticated()) {
            return;
        }
        if (TextUtils.isEmpty(content)) {
            return;
        }
        if (isBlackFriend()) {// 该用户在你的黑名单列表内
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(type);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(content);
        message.setTimeLen(timeLen);
        message.setFileSize((int) fileSize);
        message.setUpload(true);
        if (!TextUtils.isEmpty(filePath)) {
            message.setFilePath(filePath);
        }
        message.setIsReadDel(0);
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        message.setPacketId(ToolUtils.getUUID());
        sendMessage(message);
    }

    private void clickCollectionSend(CollectionEvery collection) {
        // 不管什么收藏消息类型，都可能有文字，单独发一条文字消息，
        if (!TextUtils.isEmpty(collection.getCollectContent())) {
            sendText(collection.getCollectContent());
        }
        int type = collection.getXmppType();
        if (type == Constants.TYPE_TEXT) {
            // 文字消息发出了文字就可以结束了，
            return;
        } else if (type == Constants.TYPE_IMAGE) {
            // 图片可能有多张，分开发送，
            String allUrl = collection.getUrl();
            for (String url : allUrl.split(",")) {
                clickCollectionSend(type, url, collection.getFileLength(), collection.getFileName(), collection.getFileSize());
            }
            return;
        }
        clickCollectionSend(type, collection.getUrl(), collection.getFileLength(), collection.getFileName(), collection.getFileSize());
    }

    @Override
    public void clickShake() {
        if (!ImHelper.checkXmppAuthenticated()) {
            return;
        }
        if (isBlackFriend()) {// 该用户在你的黑名单列表内
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(Constants.TYPE_SHAKE);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(getString(R.string.msg_shake));
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        message.setPacketId(ToolUtils.getUUID());
        sendMessage(message);
        shake(0);// 戳一戳效果
    }

    @Override
    public void clickGroupAssistant(GroupAssistantDetail groupAssistantDetail) {

    }

    private void shake(int type) {
        Animation shake;
        if (type == 0) {
            /*Vibrator vibrator = (Vibrator) MyApplication.getContext().getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {100, 400, 100, 400};
            vibrator.vibrate(pattern, -1);*/
            shake = AnimationUtils.loadAnimation(this, R.anim.shake_from);
        } else {
            shake = AnimationUtils.loadAnimation(this, R.anim.shake_to);
        }
        mChatContentView.startAnimation(shake);
        mChatBottomView.startAnimation(shake);
        mChatBgIv.startAnimation(shake);
    }

    /**
     * 得到选中的名片
     */
    @Override
    public void sendCardS(List<Friend> friends) {
        for (int i = 0; i < friends.size(); i++) {
            sendCard(friends.get(i));
        }
    }

    public void sendCard(Friend friend) {
        if (!ImHelper.checkXmppAuthenticated()) {
            return;
        }
        if (isBlackFriend()) {// 该用户在你的黑名单列表内
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(Constants.TYPE_CARD);
        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setContent(friend.getNickName());
        message.setIsReadDel(isReadDel);
        message.setObjectId(friend.getUserId());
        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
        message.setPacketId(ToolUtils.getUUID());
        sendMessage(message);
    }

    public void sendRed(final String type, String money, String count, final String words, String payPassword) {
        if (!ImHelper.checkXmppAuthenticated()) {
            return;
        }
        if (isBlackFriend()) {// 该用户在你的黑名单列表内
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", type);
        params.put("moneyStr", money);
        params.put("count", count);
        params.put("greetings", words);
        params.put("toUserId", mFriend.getUserId());

        HttpUtils.get().url(coreManager.getConfig().REDPACKET_SEND)
                .params(params)
                .addSecret(payPassword, money)
                .build()
                .execute(new BaseCallback<RedPacket>(RedPacket.class) {

                    @Override
                    public void onResponse(ObjectResult<RedPacket> result) {
                        RedPacket redPacket = result.getData();
                        if (result.getResultCode() != 1) {
                            // 发送红包失败，
                            ToastUtil.showToast(mContext, result.getResultMsg());
                        } else {
                            String objectId = redPacket.getId();
                            ChatMessage message = new ChatMessage();
                            message.setType(Constants.TYPE_RED);
                            message.setFromUserId(mLoginUserId);
                            message.setFromUserName(mLoginNickName);
                            message.setContent(words); // 祝福语
                            message.setFilePath(type); // 用FilePath来储存红包类型
                            message.setFileSize(redPacket.getStatus()); //用filesize来储存红包状态
                            message.setObjectId(objectId); // 红包id
                            message.setDoubleTimeSend(TimeUtils.time_current_time_double());
                            message.setPacketId(ToolUtils.getUUID());
                            sendMessage(message);
                            // 更新余额
                            CoreManager.updateMyBalance();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    @Override
    public void onInputState() {
        // 获得输入状态
        PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(this);
        boolean input = privacySetting.getIsTyping() == 1;
        if (input && ImHelper.checkXmppAuthenticated()) {
            ChatMessage message = new ChatMessage();
            // 正在输入消息
            message.setType(Constants.TYPE_INPUT);
            message.setFromUserId(mLoginUserId);
            message.setFromUserName(mLoginNickName);
            message.setToUserId(mFriend.getUserId());
            message.setDoubleTimeSend(TimeUtils.time_current_time_double());
            message.setPacketId(ToolUtils.getUUID());
            ImHelper.sendChatMessage(mFriend.getUserId(), message);
        }
    }

    /**
     * 新消息到来
     */
    @Override
    public boolean onNewMessage(String fromUserId, ChatMessage message, boolean isGroupMsg) {
        if (isGroupMsg) {
            return false;
        }

        /**
         *  因为重发机制，当对方处于弱网时，不能及时接收我方的消息回执而给我方发送了两条甚至多条一样的消息
         *  而我方则会收到两条甚至多条一样的消息存入数据库(数据库已去重)，如果我正好处于消息发送方的聊天界面
         *  则会回调多次onNewMessage方法，而该方法内又没做去重，所以会出现显示两条一模一样的消息，退出当前界面在进入
         *  该界面又只有一条的问题
         *
         */
        if (mChatMessages.size() > 0) {
            if (mChatMessages.get(mChatMessages.size() - 1).getPacketId().equals(message.getPacketId())) {// 最后一条消息的msgId==新消息的msgId
                Log.e(TAG, "收到一条重复消息");
                return false;
            }
        }
        if (mFriend.getIsDevice() == 1) {// 当前界面为我的设备界面 如果收到其他设备的转发消息，也会通知过来
            ChatMessage chatMessage = ChatMessageDao.getInstance().
                    findMsgById(mLoginUserId, mFriend.getUserId(), message.getPacketId());
            if (chatMessage == null) {
                return false;
            }
        }
        /*
         现在需要支持多点登录，此刻该条消息为我另外一台设备发送的消息，我需要将该条消息存入对应的数据库并更新界面来达到同步
         */
        if (fromUserId.equals(mLoginUserId)
                && !TextUtils.isEmpty(message.getToUserId())
                && message.getToUserId().equals(mFriend.getUserId())) {// 收到自己转发的消息且该条消息为发送给当前聊天界面的
            message.setMySend(true);
            message.setMessageState(Constants.MESSAGE_SEND_SUCCESS);
            mChatMessages.add(message);
            mChatContentView.notifyItemInserted(mChatMessages.size() - 1);
            if (message.getType() == Constants.TYPE_SHAKE) {// 戳一戳
                shake(1);
            }
            return true;
        }

        if (mFriend.getUserId().compareToIgnoreCase(fromUserId) == 0) {// 是该人的聊天消息
            //检测此条消息是否时阅后即焚的 如果是 比对一下 他设置的阅后即焚时间是否 是一只  如果不一致 要 加入一条 更改阅后即焚时间的消息
            //如果一致 则不加入

            int chageValues = ReadDelManager.getInstants().checkReadDelTimeForMe(this, message, mLoginUserId, mFriend, isReadDel);
            ChatMessage changeReadDelTime = createChangeReadDelTimeView(chageValues, false);
            if (changeReadDelTime != null && isReadDel != chageValues) {
                isReadDel = chageValues;
                changeActionBarRightStatus(isReadDel);
                mChatMessages.add(changeReadDelTime);
            }


            mChatMessages.add(message);
            mChatContentView.notifyItemInserted(mChatMessages.size() - 1);
            if (message.getType() == Constants.TYPE_SHAKE) {// 戳一戳
                shake(1);
            }
            return true;
        }
        return false;
    }

    /**
     * 创建一个 提醒 阅后即焚时间更改的view
     *
     * @param value        新值
     * @param isChangeByMe 是否是我改变了阅后即焚的值
     */
    private ChatMessage createChangeReadDelTimeView(int value, boolean isChangeByMe) {
        try {
            if (value != -1) {
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setType(Constants.TYPE_CUSTOM_CHANGE_READ_DEL_TIME);
                chatMessage.setFromUserId(mFriend.getUserId());
                chatMessage.setFromUserName(mFriend.getNickName());
                org.json.JSONObject jsonObject = new org.json.JSONObject();
                jsonObject.put("changeReadDelTime", value);
                jsonObject.put("isChangeByMe", isChangeByMe);
                chatMessage.setContent(jsonObject.toString());
                chatMessage.setObjectId("custom_view_by_read_del_time");
                chatMessage.setIsReadDel(0);
                PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(this);
                boolean isSupport = privacySetting.getMultipleDevices() == 1;
                if (isSupport) {
                    chatMessage.setFromId(MyApplication.MULTI_RESOURCE);
                } else {
                    chatMessage.setFromId("youjob");
                }
                if (mFriend.getIsDevice() == 1) {
                    chatMessage.setToUserId(userId);
                    chatMessage.setToId(mFriend.getUserId());
                } else {
                    chatMessage.setToUserId(mFriend.getUserId());
                    // sz 消息过期时间
                }
                chatMessage.setDeleteTime(-1);
                boolean isEncrypt = privacySetting.getIsEncrypt() == 1;
                if (isEncrypt) {
                    chatMessage.setIsEncrypt(1);
                } else {
                    chatMessage.setIsEncrypt(0);
                }
                chatMessage.setPacketId(ToolUtils.getUUID());
                chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                // 将消息保存在数据库了
                boolean isSavaSuccess = ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mFriend.getUserId(), chatMessage);
                Log.i(TAG, "是否保存自定义阅后即焚时间消息成功：" + isSavaSuccess);
                return chatMessage;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onMessageSendStateChange(int messageState, String msgId) {
        LogUtils.e(TAG, " messageState:" + messageState + " msgId:" + msgId);
        if (TextUtils.isEmpty(msgId)) {
            return;
        }
        for (int i = 0; i < mChatMessages.size(); i++) {
            ChatMessage msg = mChatMessages.get(i);
            if (msgId.equals(msg.getPacketId())) {
                msg.setMessageState(messageState);
                mChatContentView.notifyItemChanged(i);
                break;
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventUploadFileRate message) {
        for (int i = 0; i < mChatMessages.size(); i++) {
            if (mChatMessages.get(i).getPacketId().equals(message.getPacketId())) {
                mChatMessages.get(i).setUploadSchedule(message.getRate());
                // 不能在这里setUpload，上传完成不代表上传成功，服务器可能没有正确返回url,相当于上传失败，
                mChatContentView.notifyItemChanged(i);
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventUploadCancel message) {
        for (int i = 0; i < mChatMessages.size(); i++) {
            if (mChatMessages.get(i).getPacketId().equals(message.getPacketId())) {
                mChatMessages.remove(i);
                mChatContentView.notifyItemRemoved(i);
                ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), message.getPacketId());
                break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageVideoFile message) {
        VideoFile videoFile = new VideoFile();
        videoFile.setCreateTime(TimeUtils.f_long_2_str(System.currentTimeMillis()));
        videoFile.setFileLength(message.timelen);
        videoFile.setFileSize(message.length);
        videoFile.setFilePath(message.path);
        videoFile.setOwnerId(coreManager.getSelf().getUserId());
        VideoFileDao.getInstance().addVideoFile(videoFile);
        String filePath = message.path;
        if (TextUtils.isEmpty(filePath)) {
            ToastUtil.showToast(this, R.string.record_failed);
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            ToastUtil.showToast(this, R.string.record_failed);
            return;
        }
        sendVideo(file);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageLocalVideoFile message) {
        sendVideo(message.file);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventRedReceived message) {
        showReceiverRedLocal(message.getOpenRedpacket());
    }

    //云红包
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventCloudRedReceived message) {
        showReceiverCloudRedLocal(message.getCloudQueryRedPacket());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case BqShopActivity.BQ_CODE_REQUEST:  //表情商店回来:
                    if (data != null) {
                        boolean isRefresh = data.getBooleanExtra("isRefresh", false);
                        if (isRefresh) {
                            mChatBottomView.getMyBqBao(true, coreManager, myBqListener);
                        }
                    }
                    break;
                case REQUEST_MANAGE:
                    if (data != null) {
                        boolean isRefresh = data.getBooleanExtra("isRefresh", false);
                        if (isRefresh) {
                            mChatBottomView.getCustomBq();
                        }
                    }
                    break;
                case REQUEST_CODE_SELECT_FILE: // 系统管理器返回文件
                    String file_path = FileUtils.getPath(ChatActivity.this, data.getData());
                    Log.e(TAG, "conversionFile: " + file_path);
                    if (file_path == null) {
                        ToastUtil.showToast(mContext, R.string.tip_file_not_supported);
                    } else {
                        sendFile(new File(file_path));
                    }
                    break;
                case REQUEST_CODE_CAPTURE_PHOTO:
                    // 拍照返回
                    if (mNewPhotoUri != null) {
                        photograph(new File(mNewPhotoUri.getPath()));
                    }
                    break;
                case REQUEST_CODE_PICK_PHOTO:
                    if (data != null) {
                        boolean isOriginal = data.getBooleanExtra(PhotoPickerActivity.EXTRA_RESULT_ORIGINAL, false);
                        album(data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT), isOriginal);
                    } else {
                        ToastUtil.showToast(this, R.string.c_photo_album_failed);
                    }
                    break;
                case REQUEST_CODE_SELECT_VIDEO: {
                    // 选择视频的返回
                    if (data == null) {
                        return;
                    }
                    String json = data.getStringExtra(AppConstant.EXTRA_VIDEO_LIST);
                    List<VideoFile> fileList = JSON.parseArray(json, VideoFile.class);
                    if (fileList == null || fileList.size() == 0) {
                        // 不可到达，列表里有做判断，
                        Reporter.unreachable();
                    } else {
                        for (VideoFile videoFile : fileList) {
                            String filePath = videoFile.getFilePath();
                            if (TextUtils.isEmpty(filePath)) {
                                // 不可到达，列表里有做过滤，
                                Reporter.unreachable();
                            } else {
                                File file = new File(filePath);
                                if (!file.exists()) {
                                    // 不可到达，列表里有做过滤，
                                    Reporter.unreachable();
                                } else {
                                    sendVideo(file);
                                }
                            }
                        }
                    }
                    break;
                }
                case REQUEST_CODE_SELECT_LOCATE: // 选择位置的返回
                    double latitude = data.getDoubleExtra(AppConstant.EXTRA_LATITUDE, 0);
                    double longitude = data.getDoubleExtra(AppConstant.EXTRA_LONGITUDE, 0);
                    String address = data.getStringExtra(AppConstant.EXTRA_ADDRESS);
                    String snapshot = data.getStringExtra(AppConstant.EXTRA_SNAPSHOT);

                    if (latitude != 0 && longitude != 0 && !TextUtils.isEmpty(address)
                            && !TextUtils.isEmpty(snapshot)) {
                        sendLocate(latitude, longitude, address, snapshot);
                    } else {
                        // ToastUtil.showToast(mContext, "请把定位开启!");
                        ToastUtil.showToast(mContext, InternationalizationHelper.getString("JXLoc_StartLocNotice"));
                    }
                    break;
                case REQUEST_CODE_SEND_COLLECTION: {
                    String json = data.getStringExtra("data");
                    CollectionEvery collection = JSON.parseObject(json, CollectionEvery.class);
                    clickCollectionSend(collection);
                    break;
                }
                case REQUEST_CODE_QUICK_SEND:
                    String image = QuickSendPreviewActivity.parseResult(data);
                    sendImage(new File(image));
                    break;
                case REQUEST_CODE_INSTANTMESSAGE:
                    finish();
                    break;
                case REQUEST_CODE_SEND_CONTACT: {
                    List<Contacts> contactsList = SendContactsActivity.parseResult(data);
                    if (contactsList == null) {
                        ToastUtil.showToast(mContext, R.string.simple_data_error);
                    } else {
                        sendContacts(contactsList);
                    }
                    break;

                }
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            switch (requestCode) {
                case REQUEST_CODE_SEND_RED:
                    if (data != null && data.getExtras() != null) {
                        Bundle bundle = data.getExtras();
                        String money = bundle.getString("money"); // 金额
                        // 口令或者祝福语
                        String words = resultCode == REQUEST_CODE_SEND_RED_PT ? bundle.getString("greetings") : bundle.getString("password");
                        String count = bundle.getString("count"); // 数量
                        String type = bundle.getString("type");   // 类型
                        String payPassword = bundle.getString("payPassword");   // 支付密码，
                        sendRed(type, money, count, words, payPassword);
                    }
                    break;
                case REQUEST_CODE_SEND_RED_CLOUD://云红包
                    if (data != null && data.getExtras() != null) {
                        Bundle bundle = data.getExtras();
                        // 祝福语
                        String words = bundle.getString("greetings");
                        String type = bundle.getString("type");   // 类型
                        String objectId = bundle.getString("redPacket");//红包id
                        int status = bundle.getInt("status");//红包状态哦

                        ChatMessage message = new ChatMessage();
                        message.setType(Constants.TYPE_CLOUD_RED);
                        message.setFromUserId(mLoginUserId);
                        message.setFromUserName(mLoginNickName);
                        message.setContent(words); // 祝福语
                        message.setFilePath(type); // 用FilePath来储存红包类型
                        message.setFileSize(status); //用filesize来储存红包状态
                        message.setObjectId(objectId); // 红包id
                        message.setDoubleTimeSend(TimeUtils.time_current_time_double());
                        message.setPacketId(ToolUtils.getUUID());
                        sendMessage(message);
                    }
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    // 单张图片压缩 拍照
    private void photograph(final File file) {
        Log.e(TAG, "压缩前图片路径:" + file.getPath() + "压缩前图片大小:" + file.length() / 1024 + "KB");
        // 拍照出来的图片Luban一定支持，
        Luban.with(this)
                .load(file)
                .ignoreBy(100)     // 原图小于100kb 不压缩
                .filter(new CompressionPredicate() {
                    @Override
                    public boolean apply(String path) {
                        return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                    }
                })
                // .putGear(2)     // 设定压缩档次，默认三挡
                // .setTargetDir() // 指定压缩后的图片路径
                .setCompressListener(new OnCompressListener() { // 设置回调
                    @Override
                    public void onStart() {
                        Log.e(TAG, "开始压缩");
                    }

                    @Override
                    public void onSuccess(File file) {
                        Log.e(TAG, "压缩成功，压缩后图片位置:" + file.getPath() + "压缩后图片大小:" + file.length() / 1024 + "KB");
                        sendImage(file);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "压缩失败,原图上传");
                        sendImage(file);
                    }
                }).launch();// 启动压缩
    }

    // 多张图片压缩 相册
    private void album(ArrayList<String> stringArrayListExtra, boolean isOriginal) {
        if (isOriginal) {// 原图发送，不压缩
            Log.e(TAG, "原图发送，不压缩，开始发送");
            for (int i = 0; i < stringArrayListExtra.size(); i++) {
                sendImage(new File(stringArrayListExtra.get(i)));
            }
            Log.e(TAG, "原图发送，不压缩，发送结束");
            return;
        }

        List<File> fileList = new ArrayList<>();
        for (int i = 0; i < stringArrayListExtra.size(); i++) {
            // gif动图不压缩，
            if (stringArrayListExtra.get(i).endsWith("gif")) {
                fileList.add(new File(stringArrayListExtra.get(i)));
                stringArrayListExtra.remove(i);
            } else {
                // Luban只处理特定后缀的图片，不满足的不处理也不走回调，
                // 只能挑出来不压缩，
                List<String> lubanSupportFormatList = Arrays.asList("jpg", "jpeg", "png", "webp", "gif");
                boolean support = false;
                for (int j = 0; j < lubanSupportFormatList.size(); j++) {
                    if (stringArrayListExtra.get(i).endsWith(lubanSupportFormatList.get(j))) {
                        support = true;
                        break;
                    }
                }
                if (!support) {
                    fileList.add(new File(stringArrayListExtra.get(i)));
                    stringArrayListExtra.remove(i);
                }
            }
        }

        if (fileList.size() > 0) {
            for (File file : fileList) {// 不压缩的部分，直接发送
                sendImage(file);
            }
        }

        Luban.with(this)
                .load(stringArrayListExtra)
                .ignoreBy(100)// 原图小于100kb 不压缩
                .filter(new CompressionPredicate() {
                    @Override
                    public boolean apply(String path) {
                        return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                    }
                })
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        Log.e(TAG, "开始压缩");
                    }

                    @Override
                    public void onSuccess(File file) {
                        sendImage(file);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }).launch();// 启动压缩
    }

    /*******************************************
     * 接收到EventBus后的后续操作
     ******************************************/
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventRequert message) {
        requstImageText(message.url);
    }

    private void requstImageText(String url) {
        HttpUtils.get().url(url).build().execute(new BaseCallback<String>(String.class) {
            @Override
            public void onResponse(ObjectResult<String> result) {
                Log.d(TAG, result.getData() == null ? "" : result.getData());
            }

            @Override
            public void onError(Call call, Exception e) {
                Log.e(TAG, e.getMessage(), e);
                ToastUtil.showToast(mContext, e.getMessage());
            }
        });
//        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                Log.d(TAG, response);
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e(TAG, error.getMessage(), error);
//                ToastUtil.showToast(mContext, error.getMessage());
//            }
//        });
//        MyApplication.getInstance().getFastVolley().addDefaultRequest(null, stringRequest);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventGpu message) {// 拍照返回
        photograph(new File(message.event));
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventTransfer message) {
        if (isBlackFriend()) {// 该用户在你的黑名单列表内
            return;
        }
        mChatContentView.postDelayed(() -> {
            if (message.getChatMessage().getType() == Constants.TYPE_TRANSFER) {// 发送转账消息
                sendMessage(message.getChatMessage());
            } else if (message.getChatMessage().getType() == Constants.TYPE_CLOUD_TRANSFER) {// 发送微转账消息
                sendMessage(message.getChatMessage());
            } else {// 重发转账消息 || 确认领取
                for (int i = 0; i < mChatMessages.size(); i++) {
                    if (TextUtils.equals(mChatMessages.get(i).getPacketId(),
                            message.getChatMessage().getPacketId())) {
                        if (message.getChatMessage().getType() == TransferMoneyDetailActivity.EVENT_REISSUE_TRANSFER) {
                            ChatMessage chatMessage = mChatMessages.get(i).clone(false);
                            sendMessage(chatMessage);
                        } else {
                            mChatMessages.get(i).setFileSize(2);
                            ChatMessageDao.getInstance().updateChatMessageReceiptStatus(mLoginUserId, mFriend.getUserId(), message.getChatMessage().getPacketId());
                            mChatContentView.notifyItemChanged(i);
                        }
                    }
                }
            }
        }, 50);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEvent message) {
        Log.e(TAG, "helloEventBus  MessageEvent: " + message.message);
        if (mDelayDelMaps == null || mDelayDelMaps.isEmpty() || mChatMessages == null || mChatMessages.size() == 0) {
            return;
        }
        for (ChatMessage chatMessage : mChatMessages) {
            if (chatMessage.getFilePath().equals(message.message) && mDelayDelMaps.contains(chatMessage.getPacketId())) {
                String packedId = chatMessage.getPacketId();
                if (ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), packedId)) {
                    Log.e(TAG, "删除成功 ");
                } else {
                    Log.e(TAG, "删除失败 " + packedId);
                }
                mDelayDelMaps.remove(packedId);
                mChatContentView.removeItemMessage(chatMessage);
                break;
            }
        }
    }

    // 阅后即焚的处理
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventClickFire message) {
        Log.e(TAG, "helloEventBus: " + message.event + " ,  " + message.mChatMessage.getPacketId());
        if ("delete".equals(message.event)) {
            mDelayDelMaps.remove(message.mChatMessage.getPacketId());
            ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), message.mChatMessage.getPacketId());
            mChatContentView.removeItemMessage(message.mChatMessage);
        } else if ("delay".equals(message.event)) {
            mDelayDelMaps.add(message.mChatMessage.getPacketId());
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventSipEVent message02) {
        if (message02.number == 102) {
            // 对方在线  准备接受语音
            EventBus.getDefault().post(new MessageEventSipPreview(200, mFriend.getUserId(), true, mFriend));
        } else if (message02.number == 112) {
            // 对方在线  准备接受视频
            EventBus.getDefault().post(new MessageEventSipPreview(201, mFriend.getUserId(), false, mFriend));
        }
    }

    // 音视频会议 先不处理
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventClicAudioVideo message) {
        if (message.isauido == 0) {// 语音会议
            Intent intent1 = new Intent(getApplicationContext(), Jitsi_connecting_second.class);
            intent1.putExtra("type", 3);
            intent1.putExtra("fromuserid", message.event.getObjectId());
            intent1.putExtra("touserid", coreManager.getSelf().getUserId());
            startActivity(intent1);
        } else if (message.isauido == 1) {// 视频会议
            Intent intent1 = new Intent(getApplicationContext(), Jitsi_connecting_second.class);
            intent1.putExtra("type", 4);
            intent1.putExtra("fromuserid", message.event.getObjectId());
            intent1.putExtra("touserid", coreManager.getSelf().getUserId());
            startActivity(intent1);
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventClickable message) {
        if (message.event.isMySend()) {
            shake(0);
        } else {
            shake(1);
        }
    }

    // 发送多选消息
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventMoreSelected message) {
        List<ChatMessage> mSelectedMessageList = new ArrayList<>();
        if (message.getToUserId().equals("MoreSelectedCollection") || message.getToUserId().equals("MoreSelectedEmail")) {// 多选 收藏 || 保存
            moreSelected(false, 0);
            return;
        }
        if (message.getToUserId().equals("MoreSelectedDelete")) {// 多选 删除
            for (int i = 0; i < mChatMessages.size(); i++) {
                if (mChatMessages.get(i).isMoreSelected) {
                    if (ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), mChatMessages.get(i))) {
                        Log.e("more_selected", "删除成功");
                    } else {
                        Log.e("more_selected", "删除失败");
                    }
                    mSelectedMessageList.add(mChatMessages.get(i));
                }
            }

            String mMsgIdListStr = "";
            for (int i = 0; i < mSelectedMessageList.size(); i++) {
                if (i == mSelectedMessageList.size() - 1) {
                    mMsgIdListStr += mSelectedMessageList.get(i).getPacketId();
                } else {
                    mMsgIdListStr += mSelectedMessageList.get(i).getPacketId() + ",";
                }
            }
            deleteMessage(mMsgIdListStr);// 服务端也需要删除

            mChatMessages.removeAll(mSelectedMessageList);
        } else {// 多选 转发
            if (message.isSingleOrMerge()) {// 合并转发
                List<String> mStringHistory = new ArrayList<>();
                for (int i = 0; i < mChatMessages.size(); i++) {
                    if (mChatMessages.get(i).isMoreSelected) {
                        String body = mChatMessages.get(i).toJsonString();
                        mStringHistory.add(body);
                    }
                }
                String detail = JSON.toJSONString(mStringHistory);
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setType(Constants.TYPE_CHAT_HISTORY);
                chatMessage.setFromUserId(mLoginUserId);
                chatMessage.setFromUserName(mLoginNickName);
                chatMessage.setToUserId(message.getToUserId());
                chatMessage.setContent(detail);
                chatMessage.setMySend(true);
                chatMessage.setReSendCount(0);
                chatMessage.setSendRead(false);
                chatMessage.setIsEncrypt(0);
                chatMessage.setIsReadDel(PreferenceUtils.getInt(mContext, Constants.MESSAGE_READ_FIRE + message.getToUserId() + mLoginUserId, 0));
                String s = TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName();
                chatMessage.setObjectId(getString(R.string.chat_history_place_holder, s, mLoginNickName));
                chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                chatMessage.setPacketId(ToolUtils.getUUID());
                ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, message.getToUserId(), chatMessage);
                if (message.isGroupMsg()) {
                    ImHelper.sendMucChatMessage(message.getToUserId(), chatMessage);
                } else {
                    ImHelper.sendChatMessage(message.getToUserId(), chatMessage);
                }
                if (message.getToUserId().equals(mFriend.getUserId())) {// 转发给当前对象
                    mChatMessages.add(chatMessage);
                }
            } else {// 逐条转发
                for (int i = 0; i < mChatMessages.size(); i++) {
                    if (mChatMessages.get(i).isMoreSelected) {
                        ChatMessage chatMessage = ChatMessageDao.getInstance().findMsgById(mLoginUserId, mFriend.getUserId(), mChatMessages.get(i).getPacketId());
                        if (chatMessage.getType() == Constants.TYPE_RED) {
                            chatMessage.setType(Constants.TYPE_TEXT);
                            chatMessage.setContent(getString(R.string.msg_red_packet));
                        }
                        if (chatMessage.getType() == Constants.TYPE_READ_EXCLUSIVE) {
                            chatMessage.setType(Constants.TYPE_TEXT);
                            chatMessage.setContent(getString(R.string.msg_red_packet_exclusive));
                        }
                        if (chatMessage.getType() == Constants.TYPE_CLOUD_RED) {
                            chatMessage.setType(Constants.TYPE_TEXT);
                            chatMessage.setContent(getString(R.string.msg_red_cloud_packet));
                        } else if (chatMessage.getType() == Constants.TYPE_TRANSFER) {
                            chatMessage.setType(Constants.TYPE_TEXT);
                            chatMessage.setContent(getString(R.string.msg_red_transfer));
                        } else if (chatMessage.getType() == Constants.TYPE_CLOUD_TRANSFER) {
                            chatMessage.setType(Constants.TYPE_TEXT);
                            chatMessage.setContent(getString(R.string.micro_tip_transfer_money));
                        } else if (chatMessage.getType() >= Constants.TYPE_IS_CONNECT_VOICE
                                && chatMessage.getType() <= Constants.TYPE_EXIT_VOICE) {
                            chatMessage.setType(Constants.TYPE_TEXT);
                            chatMessage.setContent(getString(R.string.msg_video_voice));
                        } else if (chatMessage.getType() == Constants.TYPE_SHAKE) {
                            chatMessage.setType(Constants.TYPE_TEXT);
                            chatMessage.setContent(getString(R.string.msg_shake));
                        }
                        chatMessage.setFromUserId(mLoginUserId);
                        chatMessage.setFromUserName(mLoginNickName);
                        chatMessage.setToUserId(message.getToUserId());
                        chatMessage.setUpload(true);
                        chatMessage.setMySend(true);
                        chatMessage.setReSendCount(0);
                        chatMessage.setSendRead(false);
                        chatMessage.setIsEncrypt(0);
                        chatMessage.setIsReadDel(PreferenceUtils.getInt(mContext, Constants.MESSAGE_READ_FIRE + message.getToUserId() + mLoginUserId, 0));
                        chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                        chatMessage.setPacketId(ToolUtils.getUUID());
                        mSelectedMessageList.add(chatMessage);
                    }
                }

                for (int i = 0; i < mSelectedMessageList.size(); i++) {
                    ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, message.getToUserId(), mSelectedMessageList.get(i));
                    if (message.isGroupMsg()) {
                        ImHelper.sendMucChatMessage(message.getToUserId(), mSelectedMessageList.get(i));
                    } else {
                        ImHelper.sendChatMessage(message.getToUserId(), mSelectedMessageList.get(i));
                    }
                    if (message.getToUserId().equals(mFriend.getUserId())) {// 转发给当前对象
                        mChatMessages.add(mSelectedMessageList.get(i));
                    }
                }
            }
        }
        moreSelected(false, 0);
    }

    public void moreSelected(boolean isShow, int position) {
        moreSelected(isShow, position, true);
    }

    public void moreSelected(boolean isShow, int position, boolean showMoreSelectMenu) {
        if (showMoreSelectMenu) {
            mChatBottomView.showMoreSelectMenu(isShow);
        }
        if (isShow) {
            findViewById(R.id.iv_title_left).setVisibility(View.GONE);
            mTvTitleLeft.setVisibility(View.VISIBLE);
            if (!mChatMessages.get(position).getIsReadDel()) {// 非阅后即焚消息才能被选中
                mChatMessages.get(position).setMoreSelected(true);
            }
        } else {
            findViewById(R.id.iv_title_left).setVisibility(View.VISIBLE);
            mTvTitleLeft.setVisibility(View.GONE);
            for (int i = 0; i < mChatMessages.size(); i++) {
                mChatMessages.get(i).setMoreSelected(false);
            }
        }
        mChatContentView.setIsShowMoreSelect(isShow);
        mChatContentView.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageUploadChatRecord message) {
        try {
            final CreateCourseDialog dialog = new CreateCourseDialog(this, new CreateCourseDialog.CoureseDialogConfirmListener() {
                @Override
                public void onClick(String content) {
                    upLoadChatList(message.chatIds, content);
                }
            });
            dialog.show();
        } catch (Exception e) {
            // 出现过一次，复用的layout改了一个控件类型，导致findViewById强转错误，
            Reporter.unreachable(e);
        }
    }

    private void upLoadChatList(String chatIds, String name) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageIds", chatIds);
        params.put("userId", mLoginUserId);
        params.put("courseName", name);
        params.put("createTime", TimeUtils.time_current_time() + "");
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HttpUtils.get().url(coreManager.getConfig().USER_ADD_COURSE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(getApplicationContext(), getString(R.string.tip_create_cource_success));
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private void register() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(com.iimm.miliao.broadcast.OtherBroadcast.IsRead);
        intentFilter.addAction("Refresh");
        intentFilter.addAction(com.iimm.miliao.broadcast.OtherBroadcast.TYPE_INPUT);
        intentFilter.addAction(com.iimm.miliao.broadcast.OtherBroadcast.MSG_BACK);
        intentFilter.addAction(com.iimm.miliao.broadcast.OtherBroadcast.NAME_CHANGE);
        intentFilter.addAction(com.iimm.miliao.broadcast.OtherBroadcast.MULTI_LOGIN_READ_DELETE);
        intentFilter.addAction(Constants.CHAT_MESSAGE_DELETE_ACTION);
        intentFilter.addAction(Constants.SHOW_MORE_SELECT_MENU);
        intentFilter.addAction(com.iimm.miliao.broadcast.OtherBroadcast.TYPE_DELALL);
        intentFilter.addAction(Constants.CHAT_HISTORY_EMPTY);
        intentFilter.addAction(com.iimm.miliao.broadcast.OtherBroadcast.QC_FINISH);
        registerReceiver(receiver, intentFilter);
    }

    /*******************************************
     * 初始化ActionBar与其点击事件
     ******************************************/
    private void initActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        findViewById(R.id.iv_title_left).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                doBack();
            }
        });

        mTvTitleLeft = (TextView) findViewById(R.id.tv_title_left);
        mTvTitleLeft.setVisibility(View.GONE);
        mTvTitleLeft.setText(getString(R.string.cancel));
        mTvTitleLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreSelected(false, 0);
            }
        });
        mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        String remarkName = mFriend.getRemarkName();
        if (TextUtils.isEmpty(remarkName)) {
            mTvTitle.setText(mFriend.getNickName());
        } else {
            mTvTitle.setText(remarkName);
        }

        ImageView mMore = (ImageView) findViewById(R.id.iv_title_right);
        mMore.setImageResource(R.drawable.chat_more);
        mMore.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                startPersonSetting();
            }
        });

        if (mFriend.getUserId().equals(Constants.ID_SYSTEM_MESSAGE)
                || mFriend.getIsDevice() == 1) {// 公众号与我的手机 || 我的电脑 更多
            mMore.setVisibility(View.INVISIBLE);
        }

        // 加载聊天背景
        mChatBgIv = findViewById(R.id.chat_bg);
        loadBackdrop();
    }

    public void loadBackdrop() {
        String mChatBgPath = PreferenceUtils.getString(this, Constants.SET_CHAT_BACKGROUND_PATH
                + mFriend.getUserId() + mLoginUserId, "reset");

        String mChatBg = PreferenceUtils.getString(this, Constants.SET_CHAT_BACKGROUND
                + mFriend.getUserId() + mLoginUserId, "reset");

        if (TextUtils.isEmpty(mChatBgPath)
                || mChatBg.equals("reset")) {// 未设置聊天背景或者还原了聊天背景
            mChatBgIv.setImageDrawable(null);
            return;
        }

        File file = new File(mChatBgPath);
        if (file.exists()) {// 加载本地
            if (mChatBgPath.toLowerCase().endsWith("gif")) {
                try {
                    GifDrawable gifDrawable = new GifDrawable(file);
                    mChatBgIv.setImageDrawable(gifDrawable);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Glide.with(ChatActivity.this)
                        .load(file)
                        .error(R.drawable.fez)
                        .into(mChatBgIv);
            }
        } else {// 加载网络
            Glide.with(ChatActivity.this)
                    .load(mChatBg)
                    .error(getResources().getDrawable(R.color.chat_bg))
                    .into(mChatBgIv);
        }
    }

    /*******************************************
     * 获取公众号菜单&&获取好友在线状态
     ******************************************/
    private void initFriendState() {
        if (mFriend.getIsDevice() == 1) {
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mFriend.getUserId());

        HttpUtils.get().url(coreManager.getConfig().USER_GET_URL)
                .params(params)
                .build()
                .execute(new BaseCallback<User>(User.class) {
                    @Override
                    public void onResponse(ObjectResult<User> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            User user = result.getData();
                            if (user.getUserType() == 2) {
                                // 公众号,获取公众号菜单
                                initSpecialMenu();
                                return;
                            }
                            String name = mTvTitle.getText().toString();
                            switch (user.getOnlinestate()) {
                                case 0:
                                    mTvTitle.setText(name + "(" + InternationalizationHelper.getString("JX_OffLine") + ")");
                                    break;
                                case 1:
                                    mTvTitle.setText(name + "(" + InternationalizationHelper.getString("JX_OnLine") + ")");
                                    break;
                            }

                            if (user.getFriends() != null) {// 更新消息免打扰状态 && 更新消息保存天数...
                                FriendDao.getInstance().updateFriendPartStatus(mFriend.getUserId(), user);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private void initSpecialMenu() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mFriend.getUserId());

        HttpUtils.get().url(coreManager.getConfig().USER_GET_PUBLIC_MENU)
                .params(params)
                .build()
                .execute(new ListCallback<PublicMenu>(PublicMenu.class) {
                    @Override
                    public void onResponse(ArrayResult<PublicMenu> result) {
                        List<PublicMenu> data = result.getData();
                        if (data != null && data.size() > 0) {
                            mChatBottomView.fillRoomMenu(data);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void processEventBusMsg(EventBusMsg eventBusMsg) {
        if (eventBusMsg.getMessageType() == Constants.EVENTBUS_MSG_AUTH_CONNECT_ING) {
        } else if (eventBusMsg.getMessageType() == Constants.EVENTBUS_MSG_AUTH_SUCCESS) {
            //重新登录 成功 更新下服务器从30分钟前到现在的最新消息
            long lastAuthNotTime = PreferenceUtils.getLong(this, "AUTH_NOT_LAST_TIME", 0);
            if (lastAuthNotTime == 0) {
                lastAuthNotTime = (TimeUtils.time_current_time() - (60 * 30)) * 1000;
            } else if (lastAuthNotTime > mFriend.getDownloadTime()) {
                lastAuthNotTime = mFriend.getDownloadTime() * 1000;
            }
            getChatHistory(lastAuthNotTime);
        } else if (eventBusMsg.getMessageType() == Constants.EVENTBUS_MSG_AUTH_NOT) {
            //掉线 了
            PreferenceUtils.putLong(this, "AUTH_NOT_LAST_TIME", TimeUtils.time_current_time() * 1000); //已毫秒为单位存储的
        } else if (eventBusMsg.getMessageType() == Constants.EVENTBUS_TRANSPOND_REFRESH_UI) {
            moreSelected(false, 0);
        }
    }

    public void synchronizeChatHistory() {
        // 在调用该方法的时候，用户可能还会去下拉获取漫游，导致出现了重复的消息
        // 当该方法在调用时，禁止用户下拉
        refresh.setEnableRefresh(false);

        long startTime;
//        String chatSyncTimeLen = String.valueOf(PrivacySettingHelper.getPrivacySettings(this).getChatSyncTimeLen());
//        if (Double.parseDouble(chatSyncTimeLen) == -2) {// 不同步
//            refresh.setEnableRefresh(true);
//            FriendDao.getInstance().updateDownloadTime(mLoginUserId, mFriend.getUserId(), mFriend.getTimeSend());
//            return;
//        }
//        if (Double.parseDouble(chatSyncTimeLen) == -1 || Double.parseDouble(chatSyncTimeLen) == 0) {// 同步 永久 startTime == downloadTime
            startTime = mFriend.getDownloadTime();
//        } else {
//            long syncTimeLen = (long) (Double.parseDouble(chatSyncTimeLen) * 24 * 60 * 60);// 得到消息同步时长
//            if (mFriend.getTimeSend() - mFriend.getDownloadTime() <= syncTimeLen) {// 未超过消息同步时长
//                startTime = mFriend.getDownloadTime();
//            } else {// 超过消息同步时长，只同步时长内的消息
//                startTime = mFriend.getTimeSend() - syncTimeLen;
//            }
//        }
        getChatHistory(startTime * 1000);
    }

    private void getChatHistory(long startTime) {
        if (mTvTitle == null || mChatMessages == null || mChatContentView == null) {
            return;
        }
        Map<String, String> params = new HashMap();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("receiver", mFriend.getUserId());
        params.put("startTime", String.valueOf(startTime));// 2010-01-01 00:00:00  服务端返回的数据为倒序返回
        params.put("endTime", String.valueOf(TimeUtils.time_current_time() * 1000));
        params.put("pageSize", String.valueOf(Constants.MSG_ROMING_PAGE_SIZE));// 尽量传一个大的值 一次性拉下来
        // params.put("pageIndex", "0");

        HttpUtils.get().url(coreManager.getConfig().GET_CHAT_MSG)
                .params(params)
                .build()
                .execute(new ListCallback<ChatRecord>(ChatRecord.class) {
                    @Override
                    public void onResponse(ArrayResult<ChatRecord> result) {
                        FriendDao.getInstance().updateDownloadTime(mLoginUserId, mFriend.getUserId(), mFriend.getTimeSend());
                        final List<ChatRecord> chatRecordList = result.getData();
                        if (chatRecordList != null && chatRecordList.size() > 0) {
                            chatMessages = new ArrayList<>();

                            for (int i = 0; i < chatRecordList.size(); i++) {
                                ChatRecord data = chatRecordList.get(i);
                                String messageBody = data.getBody();
                                messageBody = messageBody.replaceAll("&quot;", "\"");
                                ChatMessage chatMessage = new ChatMessage(messageBody);

                                if (!TextUtils.isEmpty(chatMessage.getFromUserId()) &&
                                        chatMessage.getFromUserId().equals(mLoginUserId)) {
                                    chatMessage.setMySend(true);
                                }

                                chatMessage.setSendRead(data.getIsRead() > 0); // 单聊的接口有返回是否已读，
                                // 漫游的默认已上传
                                chatMessage.setUpload(true);
                                chatMessage.setUploadSchedule(100);
                                chatMessage.setMessageState(Constants.MESSAGE_SEND_SUCCESS);

                                if (TextUtils.isEmpty(chatMessage.getPacketId())) {
                                    if (!TextUtils.isEmpty(data.getMessageId())) {
                                        chatMessage.setPacketId(data.getMessageId());
                                    } else {
                                        chatMessage.setPacketId(ToolUtils.getUUID());
                                    }
                                }
                                if (packetIdMap.get(chatMessage.getPacketId()) != null) {
                                    continue;
                                } else {
                                    packetIdMap.put(chatMessage.getPacketId(), mFriend.getUserId());
                                }
                                if (ChatMessageDao.getInstance().roamingMessageFilter(chatMessage.getType())) {
                                    ChatMessageDao.getInstance().decryptDES(chatMessage);
                                    ChatMessageDao.getInstance().handlerRoamingSpecialMessage(chatMessage);
                                    if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mFriend.getUserId(), chatMessage)) {
                                        chatMessages.add(chatMessage);
                                    }
                                }
                            }

                            for (int i = chatMessages.size() - 1; i >= 0; i--) {
                                mChatMessages.add(chatMessages.get(i));
                            }
                            // 有可能本地已经发送或接收到了消息，需要对mChatMessages重新排序
                            Comparator<ChatMessage> comparator = (c1, c2) -> (int) (c1.getDoubleTimeSend() - c2.getDoubleTimeSend());
                            Collections.sort(mChatMessages, comparator);
                            mChatContentView.notifyDataSetInvalidated(true);
                            refresh.setEnableRefresh(true);
                        } else {
                            refresh.setEnableRefresh(true);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        refresh.setEnableRefresh(true);
                        // 2021-01-26 去掉异常提示
                        // ToastUtil.showErrorData(ChatActivity.this);
                    }
                });
    }


    public void getNetSingle() {
        Map<String, String> params = new HashMap();
        long endTime;
        if (mChatMessages != null && mChatMessages.size() > 0) {// 本地有数据，截止时间为本地最早的一条消息的timeSend
            endTime = mChatMessages.get(0).getTimeSend();
        } else {// 本地无数据，截止时间为当前时间
            endTime = TimeUtils.time_current_time();
        }
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("receiver", mFriend.getUserId());
        params.put("startTime", "1262275200000");// 2010-01-01 00:00:00  服务端返回的数据为倒序返回
        params.put("endTime", String.valueOf(endTime * 1000));
        params.put("pageSize", String.valueOf(Constants.MSG_ROMING_PAGE_SIZE));
        params.put("pageIndex", "0");

        HttpUtils.get().url(coreManager.getConfig().GET_CHAT_MSG)
                .params(params)
                .build()
                .execute(new ListCallback<ChatRecord>(ChatRecord.class) {
                    @Override
                    public void onResponse(ArrayResult<ChatRecord> result) {
                        List<ChatRecord> chatRecordList = result.getData();
                        if (chatRecordList != null && chatRecordList.size() > 0) {
                            long currTime = TimeUtils.time_current_time();
                            for (int i = 0; i < chatRecordList.size(); i++) {
                                ChatRecord data = chatRecordList.get(i);
                                String messageBody = data.getBody();
                                messageBody = messageBody.replaceAll("&quot;", "\"");
                                ChatMessage chatMessage = new ChatMessage(messageBody);
                                // 有一种情况，因为服务器1个小时才去删除一次过期消息，所以可能会拉到已过期的时间
                                if (chatMessage.getIsReadDelByInt() > 0 && chatMessage.getDeleteTime() > 1 && chatMessage.getDeleteTime() < currTime) {
                                    // 已过期的消息,扔掉
                                    continue;
                                }
                                if (!TextUtils.isEmpty(chatMessage.getFromUserId()) &&
                                        chatMessage.getFromUserId().equals(mLoginUserId)) {
                                    chatMessage.setMySend(true);
                                }
                                chatMessage.setSendRead(data.getIsRead() > 0); // 单聊的接口有返回是否已读，
                                // 漫游的默认已上传
                                chatMessage.setUpload(true);
                                chatMessage.setUploadSchedule(100);
                                chatMessage.setMessageState(Constants.MESSAGE_SEND_SUCCESS);

                                if (TextUtils.isEmpty(chatMessage.getPacketId())) {
                                    if (!TextUtils.isEmpty(data.getMessageId())) {
                                        chatMessage.setPacketId(data.getMessageId());
                                    } else {
                                        chatMessage.setPacketId(ToolUtils.getUUID());
                                    }
                                }
                                if (ChatMessageDao.getInstance().roamingMessageFilter(chatMessage.getType())) {
                                    ChatMessageDao.getInstance().saveRoamingChatMessage(mLoginUserId, mFriend.getUserId(), chatMessage);
                                }
                            }
                            mHasMoreData = chatRecordList.size() == Constants.MSG_ROMING_PAGE_SIZE;
                            notifyChatAdapter();
                        } else {
                            mHasMoreData = false;
                            refresh.finishRefresh();
                            refresh.setEnableRefresh(false);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    private void notifyChatAdapter() {
        if (mChatMessages.size() > 0) {
            mMinId = mChatMessages.get(0).getTimeSend();
        } else {
            ChatMessage chat = ChatMessageDao.getInstance().getLastChatMessage(mLoginUserId, mFriend.getUserId());
            if (chat != null && chat.getTimeSend() != 0) {
                mMinId = chat.getTimeSend() + 2;
            } else {
                mMinId = TimeUtils.time_current_time();
            }
        }
        // 代码等跑到这里来说明 mMinId 一定没有查到数据，同步了漫游之后我们再次使用 mMinId 去查询一下数据
        List<ChatMessage> chatLists = ChatMessageDao.getInstance().getSingleChatMessages(mLoginUserId,
                mFriend.getUserId(), mMinId, mPageSize);
        if (chatLists == null || chatLists.size() == 0) {
            mHasMoreData = false;
            refresh.finishRefresh();
            refresh.setEnableRefresh(false);
            return;
        }
        List<ChatMessage> messages = new ArrayList<>();
        messages.clear();
        for (int i = 0; i < chatLists.size(); i++) {
            ChatMessage message = chatLists.get(i);
            if (packetIdMap.get(message.getPacketId()) != null) {
                continue;
            } else {
                packetIdMap.put(message.getPacketId(), mFriend.getUserId());
            }
            messages.add(0, message);
        }
        // 根据timeSend进行排序
       /* Collections.sort(mChatMessages, new Comparator<ChatMessage>() {
            @Override
            public int compare(ChatMessage o1, ChatMessage o2) {
                return (int) (o1.getDoubleTimeSend() - o2.getDoubleTimeSend());
            }
        });*/
        mChatMessages.addAll(0, messages);
        mChatContentView.notifyItemInserted(0, messages.size());
        refresh.finishRefresh();
        if (!mHasMoreData) {
            refresh.setEnableRefresh(false);
        }
    }

    /*******************************************
     * 转发&&拦截
     ******************************************/
    private void instantChatMessage() {
        if (!TextUtils.isEmpty(instantMessage)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String toUserId = getIntent().getStringExtra("fromUserId");
                    ChatMessage chatMessage = ChatMessageDao.getInstance().findMsgById(mLoginUserId, toUserId, instantMessage);
                    chatMessage.setFromUserId(mLoginUserId);
                    chatMessage.setFromUserName(mLoginNickName);
                    chatMessage.setToUserId(mFriend.getUserId());
                    chatMessage.setUpload(true);
                    chatMessage.setMySend(true);
                    chatMessage.setReSendCount(5);
                    chatMessage.setSendRead(false);
                    // 因为该消息的原主人可能开启了消息传输加密，我们对于content字段解密后存入了数据库，但是isEncrypt字段并未改变
                    // 如果我们将此消息转发给另一人，对方可能会对我方已解密的消息再次进行解密
                    chatMessage.setIsEncrypt(0);
                    chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
                    chatMessage.setPacketId(ToolUtils.getUUID());
                    mChatMessages.add(chatMessage);
                    mChatContentView.notifyItemInserted(mChatMessages.size() - 1);

                    ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, mFriend.getUserId(), chatMessage);
                    ImHelper.sendChatMessage(mFriend.getUserId(), chatMessage);
                    instantMessage = null;
                }
            }, 1000);
        }
    }

    /**
     * 黑名单中的人
     *
     * @return
     */
    public boolean isBlackFriend() {
        if (interprect()) {// 该用户在你的黑名单列表内
            ToastUtil.showToast(this, getString(R.string.tip_remote_in_black));
            // 移除掉该条消息
            return true;
        } else {
            return false;
        }
    }

    public boolean interprect() {
        for (Friend friend : mBlackList) {
            if (friend.getUserId().equals(mFriend.getUserId())) {
                return true;
            }
        }
        return false;
    }

    /*******************************************
     * 接收到广播后的后续操作
     ******************************************/
    public class RefreshBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(OtherBroadcast.IsRead)) {
                // 收到已读的广播 单聊
                Bundle bundle = intent.getExtras();
                String packetId = bundle.getString("packetId");
                boolean isReadChange = bundle.getBoolean("isReadChange");
                for (int i = 0; i < mChatMessages.size(); i++) {
                    ChatMessage msg = mChatMessages.get(i);
                    if (msg.getPacketId().equals(packetId)) {
                        msg.setSendRead(true);// 更新为已读
                        msg.setMessageState(Constants.MESSAGE_SEND_SUCCESS);
                        if (isReadChange) {// 阅后即焚已读 且本地数据库已经修改
                            ChatMessage msgById = ChatMessageDao.getInstance().findMsgById(mLoginUserId, mFriend.getUserId(), packetId);
                            if (msgById != null) {
                                if (msg.getType() == Constants.TYPE_VOICE) {
                                    if (!TextUtils.isEmpty(VoicePlayer.instance().getVoiceMsgId())
                                            && packetId.equals(VoicePlayer.instance().getVoiceMsgId())) {// 对方查看该语音时，我正在播放... 需要停止播放
                                        VoicePlayer.instance().stop();
                                    }
                                } else if (msg.getType() == Constants.TYPE_VIDEO) {
                                    if (!TextUtils.isEmpty(JCMediaManager.CURRENT_PLAYING_URL)
                                            && msg.getContent().equals(JCMediaManager.CURRENT_PLAYING_URL)) {// 对方查看该视频时，我正在播放... 需要退出全屏、停止播放
                                        JCVideoPlayer.releaseAllVideos();
                                    }
                                }

                                msg.setType(msgById.getType());
                                msg.setContent(msgById.getContent());
                            }
                        }
                        mChatContentView.notifyItemChanged(i);

                        // 收到已读，将离线修改为在线
                        String titleContent = mTvTitle.getText().toString();
                        if (titleContent.contains(InternationalizationHelper.getString("JX_OffLine"))) {
                            String changeTitleContent = titleContent.replace(InternationalizationHelper.getString("JX_OffLine"),
                                    InternationalizationHelper.getString("JX_OnLine"));
                            mTvTitle.setText(changeTitleContent);
                        }
                        break;
                    }
                }
            } else if (action.equals("Refresh")) {
                Bundle bundle = intent.getExtras();
                String packetId = bundle.getString("packetId");
                String fromId = bundle.getString("fromId");
                int type = bundle.getInt("type");
               /* if (type == Constants.TYPE_INPUT && mFriend.getUserId().equals(fromId)) {
                    // 对方正在输入...
                    nameTv.setText(InternationalizationHelper.getString("JX_Entering"));
                    time.cancel();
                    time.start();
                }*/
                // 这里表示正在聊天的时候，收到新消息，重新适配一下数据可以立即返回已读回执
                for (int i = 0; i < mChatMessages.size(); i++) {
                    ChatMessage msg = mChatMessages.get(i);
                    // 碰到packetId为空的就是刚刚加进来的消息
                    if (msg.getPacketId() == null) {
                        // 找到该消息，把已读标志设置为false，然后适配数据的时候就可以发现它，就可以回执已读了
                        msg.setSendRead(false); // 收到新消息，默认未读
                        msg.setFromUserId(mFriend.getUserId());
                        msg.setPacketId(packetId);
                        mChatContentView.notifyItemChanged(i);
                        break;
                    }
                }

            } else if (action.equals(com.iimm.miliao.broadcast.OtherBroadcast.TYPE_INPUT)) {
                String fromId = intent.getStringExtra("fromId");
                if (mFriend.getUserId().equals(fromId)) {
                    // 对方正在输入...
                    Log.e(TAG, "对方正在输入...");
                    mTvTitle.setText(InternationalizationHelper.getString("JX_Entering"));
                    time.cancel();
                    time.start();
                }
            } else if (action.equals(com.iimm.miliao.broadcast.OtherBroadcast.MSG_BACK)) {
                String packetId = intent.getStringExtra("packetId");
                if (TextUtils.isEmpty(packetId)) {
                    return;
                }
                int pos = 0;
                for (int i = 0; i < mChatMessages.size(); i++) {
                    if (packetId.equals(mChatMessages.get(i).getPacketId())) {
                        if (mChatMessages.get(i).getType() == Constants.TYPE_VOICE
                                && !TextUtils.isEmpty(VoicePlayer.instance().getVoiceMsgId())
                                && packetId.equals(VoicePlayer.instance().getVoiceMsgId())) {// 语音 && 正在播放的msgId不为空 撤回的msgId==正在播放的msgId
                            // 停止播放语音
                            VoicePlayer.instance().stop();
                        }
                        pos = i;
                        break;
                    }
                }
                ChatMessage chat = ChatMessageDao.getInstance().findMsgById(mLoginUserId, mFriend.getUserId(), packetId);
                if (chat != null) {
                    if (mChatMessages.size() > pos) {
                        mChatMessages.get(pos).setType(chat.getType());
                        mChatMessages.get(pos).setContent(chat.getContent());
                        mChatContentView.notifyItemChanged(pos);
                    }
                } else {
                    if (mChatMessages.size() > pos) {
                        mChatMessages.remove(pos);//集合移除该条
                        mChatContentView.notifyItemRemoved(pos);//通知移除该条
                    }
                }

            } else if (action.equals(com.iimm.miliao.broadcast.OtherBroadcast.NAME_CHANGE)) {// 修改备注名
                mFriend = FriendDao.getInstance().getFriend(mLoginUserId, mFriend.getUserId());
                String s = mTvTitle.getText().toString();
                if (s.contains(InternationalizationHelper.getString("JX_OnLine"))) {
                    mTvTitle.setText(TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName()
                            + "(" + InternationalizationHelper.getString("JX_OnLine") + ")");
                } else {
                    mTvTitle.setText(TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName()
                            + "(" + InternationalizationHelper.getString("JX_OffLine") + ")");
                }
            } else if (action.equals(com.iimm.miliao.broadcast.OtherBroadcast.MULTI_LOGIN_READ_DELETE)) {// 兼容 多点登录 阅后即焚 其他端已读了该条消息
                String packet = intent.getStringExtra("MULTI_LOGIN_READ_DELETE_PACKET");
                if (!TextUtils.isEmpty(packet)) {

                    for (int i = 0; i < mChatMessages.size(); i++) {
                        if (mChatMessages.get(i).getPacketId().equals(packet)) {
                            mChatMessages.remove(i);
                            mChatContentView.notifyItemRemoved(i);
                            break;
                        }
                    }
                }
            } else if (action.equals(Constants.CHAT_MESSAGE_DELETE_ACTION)) {

                if (mChatMessages == null || mChatMessages.size() == 0) {
                    return;
                }

                // 用户手动删除
                int position = intent.getIntExtra(Constants.CHAT_REMOVE_MESSAGE_POSITION, -1);
                if (position >= 0 && position < mChatMessages.size()) { // 合法的postion
                    ChatMessage message = mChatMessages.get(position);
                    deleteMessage(message.getPacketId());// 服务端也需要删除
                    if (ChatMessageDao.getInstance().deleteSingleChatMessage(mLoginUserId, mFriend.getUserId(), message)) {
                        mChatMessages.remove(position);
                        mChatContentView.notifyItemRemoved(position);
                        Toast.makeText(mContext, InternationalizationHelper.getString("JXAlert_DeleteOK"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, R.string.delete_failed, Toast.LENGTH_SHORT).show();
                    }
                }

            } else if (action.equals(Constants.SHOW_MORE_SELECT_MENU)) {// 显示多选菜单
                int position = intent.getIntExtra(Constants.CHAT_SHOW_MESSAGE_POSITION, 0);
                moreSelected(true, position, true);
            } else if (action.equals(com.iimm.miliao.broadcast.OtherBroadcast.TYPE_DELALL)) {
                // 被拉黑 || 删除  @see XChatManger 190
                // 好友被后台删除，xmpp 512,
                String toUserId = intent.getStringExtra("toUserId");
                // 只处理正在聊天对象是删除自己的人的情况，
                if (Objects.equals(mFriend.getUserId(), toUserId)) {
                    String content = intent.getStringExtra("content");
                    if (!TextUtils.isEmpty(content)) {
                        ToastUtil.showToast(mContext, content);
                    }
                    Intent mainIntent = new Intent(mContext, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
            } else if (action.equals(Constants.CHAT_HISTORY_EMPTY)) {// 清空聊天记录
                mChatMessages.clear();
                mChatContentView.notifyDataSetChanged();
            } else if (action.equals(com.iimm.miliao.broadcast.OtherBroadcast.QC_FINISH)) {
                int mOperationCode = intent.getIntExtra("Operation_Code", 0);
                if (mOperationCode == 1) {// 更换聊天背景成功 更新当前页面
                    loadBackdrop();
                } else {// 快速创建群组成功 关闭当前页面
                    finish();
                }
            }
        }
    }

    private ChatFaceView.CustomBqListener customBqListener = new ChatFaceView.CustomBqListener() {
        @Override
        public void onClickViewSendBq(boolean b, String custom, String customBq, String fileUrl, String s) {
           /* if (!TextUtils.isEmpty(fileUrl) && fileUrl.endsWith("gif")) {
                sendGif(fileUrl);
            } else */
            if (!TextUtils.isEmpty(fileUrl)) {
                sendCollection(fileUrl);
            } else {
                sendCollection(fileUrl == null ? "" : fileUrl);
            }
        }

        @Override
        public void goCustomBqManageActivity() {
            Intent intent = new Intent(ChatActivity.this, CustomBqManageActivity.class);
            startActivityForResult(intent, REQUEST_MANAGE);
        }
    };
    private ChatFaceView.BqKeyBoardListener bqKeyBoardListener = v -> BqShopActivity.start(ChatActivity.this, BqShopActivity.BQ_CODE_REQUEST);
    private ChatFaceView.MyBqListener myBqListener = new ChatFaceView.MyBqListener() {
        @Override
        public void onClickViewSendBq(boolean b, String id, String emoMean, String fileUrl, String thumbnailUrl) {
           /* if (!TextUtils.isEmpty(fileUrl) && fileUrl.endsWith("gif")) {
                sendGif(fileUrl);
            } else */
            if (!TextUtils.isEmpty(fileUrl)) {
                sendCollection(fileUrl);
            } else {
                sendCollection(fileUrl == null ? "" : fileUrl);
            }
        }
    };


}
