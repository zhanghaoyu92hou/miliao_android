package com.iimm.miliao.ui.share;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.iimm.miliao.AppConfig;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.ShareBean;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.helper.LoginHelper;
import com.iimm.miliao.ui.MainActivity;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.message.InstantMessageConfirm;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DeviceInfoUtil;
import com.iimm.miliao.util.Md5Util;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ToolUtils;
import com.iimm.miliao.view.LoadFrame;
import com.iimm.miliao.view.MessageAvatar;
import com.iimm.miliao.xmpp.ListenerManager;
import com.iimm.miliao.xmpp.listener.ChatMessageListener;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 分享 最近联系人
 */
public class ShareNearChatFriend extends BaseActivity implements OnClickListener, ChatMessageListener {
    private ListView mShareLv;
    private List<Friend> mFriends;

    private InstantMessageConfirm menuWindow;
    private LoadFrame mLoadFrame;

    private String mShareContent;
    private ShareBean mShareBean;
    private ChatMessage mShareChatMessage;

    private String mTigName;
    private String mTigImage;
    private String mTigAppId;
    private String mTigAppSecret;
    private String mTigTitle;
    private String mTigSubTitle;
    private String mTigUrl;
    private String mTigImageUrl;


    private boolean isNeedExecuteLogin;
    private BroadcastReceiver mShareBroadCast = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!TextUtils.isEmpty(intent.getAction())
                    && intent.getAction().equals(ShareBroadCast.ACTION_FINISH_ACTIVITY)) {
                finish();
            }
        }
    };

    public ShareNearChatFriend() {
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messageinstant);

        // 已进入分享界面
        ShareConstant.IS_SHARE_S_COME = true;

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(ShareConstant.TIG_BUNDLE);
        mTigName = bundle.getString(ShareConstant.TIG_NAME);
        mTigImage = bundle.getString(ShareConstant.TIG_IMAGE);
        mTigAppId = bundle.getString(ShareConstant.TIG_APP_ID);
        mTigAppSecret = bundle.getString(ShareConstant.TIG_APP_SECRET);
        mTigTitle = bundle.getString(ShareConstant.TIG_TITLE);
        mTigSubTitle = bundle.getString(ShareConstant.TIG_SUBTITLE);
        mTigUrl = bundle.getString(ShareConstant.TIG_URL);
        mTigImageUrl = bundle.getString(ShareConstant.TIG_IMAGE_URL);

        if (TextUtils.isEmpty(mTigAppId)) {
            ToastUtil.showToast(ShareNearChatFriend.this, "AppId不能为空");
            return;
        } else if (TextUtils.isEmpty(mTigAppSecret)) {
            ToastUtil.showToast(ShareNearChatFriend.this, "AppSecret不能为空");
            return;
        } else if (TextUtils.isEmpty(mTigName)) {
            ToastUtil.showToast(ShareNearChatFriend.this, "名称不能为空");
            return;
        } else if (TextUtils.isEmpty(mTigImage)) {
            ToastUtil.showToast(ShareNearChatFriend.this, "logo不能为空");
            return;
        }
        ShareBean shareBean = new ShareBean(mTigAppId, mTigAppSecret, mTigName, mTigImage, mTigTitle, mTigSubTitle, mTigUrl, mTigImageUrl);
        mShareContent = new Gson().toJson(shareBean);
        mShareBean = JSON.parseObject(mShareContent, ShareBean.class);


        // 判断本地登录状态
        int userStatus = LoginHelper.prepareUser(mContext, coreManager);
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
            startActivity(new Intent(mContext, ShareLoginActivity.class));
            finish();
            return;
        }
        ImHelper.checkXmppAuthenticated();
        initActionBar();
        loadData();
        initView();

        ListenerManager.getInstance().addChatMessageListener(this);
        getApplicationContext().registerReceiver(mShareBroadCast, new IntentFilter(ShareBroadCast.ACTION_FINISH_ACTIVITY));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ListenerManager.getInstance().removeChatMessageListener(this);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(this);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.most_recent_contact));
    }

    private void loadData() {
        mFriends = FriendDao.getInstance().getNearlyFriendMsg(coreManager.getSelf().getUserId());
        for (int i = 0; i < mFriends.size(); i++) {
            if (mFriends.get(i).getUserId().equals(Constants.ID_NEW_FRIEND_MESSAGE)) {
                mFriends.remove(i);
            }
        }
    }

    private void initView() {
        findViewById(R.id.tv_create_newmessage).setOnClickListener(this);
        findViewById(R.id.ll_send_life_circle).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_send_life_circle).setOnClickListener(this);

        mShareLv = findViewById(R.id.lv_recently_message);
        mShareLv.setAdapter(new MessageRecentlyAdapter());
        mShareLv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                Friend friend = mFriends.get(position);
                showPopuWindow(view, friend);
            }
        });
    }

    private void showPopuWindow(View view, Friend friend) {
        menuWindow = new InstantMessageConfirm(this, new ClickListener(friend), friend);
        menuWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    @Override
    public void onMessageSendStateChange(int messageState, String msgId) {
        if (TextUtils.isEmpty(msgId)) {
            return;
        }
        // 更新消息Fragment的广播
        MsgBroadcast.broadcastMsgUiUpdate(mContext);
        if (mShareChatMessage != null && mShareChatMessage.getPacketId().equals(msgId)) {
            if (messageState == Constants.MESSAGE_SEND_SUCCESS) {// 发送成功
                if (mLoadFrame != null) {
                    mLoadFrame.change();
                }
            }
        }
    }

    @Override
    public boolean onNewMessage(String fromUserId, ChatMessage message, boolean isGroupMsg) {
        return false;
    }


    /**
     * 事件的监听
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_title_left:
                finish();
                break;
            case R.id.tv_create_newmessage:
                verifyAccount(1, null);
                break;
            case R.id.tv_send_life_circle:
                verifyAccount(2, null);
                break;
            default:
                break;
        }
    }

    /**
     * 验证当前应用
     */
    private void verifyAccount(int type, Friend friend) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        String time = String.valueOf(System.currentTimeMillis() / 1000);
        String secret = Md5Util.toMD5(mShareBean.getAppId() + Md5Util.toMD5(time) + Md5Util.toMD5(mShareBean.getAppSecret()));
        Map<String, String> params = new ArrayMap<>();
        params.put("appId", mShareBean.getAppId());
        params.put("appSecret", mShareBean.getAppSecret());
        params.put("secret", secret);
        params.put("time", time);
        HttpUtils.getNoSecret().url(coreManager.getConfig().AUTHORIZATION)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            verificationShare(type, friend);
                        } else {
                            DialogHelper.dismissProgressDialog();
                            backtrack(result.getResultMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        backtrack(e.getMessage());
                    }
                });
    }

    private void verificationShare(final int type, Friend friend) {
        Map<String, String> params = new HashMap<>();
        String time = String.valueOf(TimeUtils.time_current_time());
        String secret = Md5Util.toMD5(AppConfig.apiKey + mShareBean.getAppId() + coreManager.getSelf().getUserId() +
                Md5Util.toMD5(coreManager.getSelfStatus().accessToken + time) + Md5Util.toMD5(mShareBean.getAppSecret()));
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        params.put("type", String.valueOf(2));// 1.授权 2.分享 3.支付
        params.put("appId", mShareBean.getAppId());
        params.put("appSecret", mShareBean.getAppSecret());
        params.put("time", time);
        params.put("secret", secret);

        HttpUtils.get().url(coreManager.getConfig().SDK_OPEN_AUTH_INTERFACE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            if (type == 1) {// 选择好友
                                Intent intent = new Intent(ShareNearChatFriend.this, ShareNewFriend.class);
                                intent.putExtra(ShareConstant.EXTRA_SHARE_CONTENT, mShareContent);
                                startActivity(intent);
                            } else if (type == 2) {// 生活圈
                                Intent intent = new Intent(ShareNearChatFriend.this, ShareLifeCircleActivity.class);
                                intent.putExtra(ShareConstant.EXTRA_SHARE_CONTENT, mShareContent);
                                startActivity(intent);
                            } else {// 直接发送
                                share(friend);
                            }
                        } else {// 没有 ‘分享’ 权限
                            backtrack(getString(R.string.tip_no_share_permission));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        backtrack(e.getMessage());
                    }
                });
    }

    public void share(Friend friend) {
        if (friend.getRoomFlag() != 0) {
            if (TimeUtils.isSilenceInGroup(friend)) {// 禁言时间 > 当前时间 禁言还未结束
                DialogHelper.tip(mContext, getString(R.string.tip_forward_ban));
                return;
            } else if (friend.getGroupStatus() == 1) {
                DialogHelper.tip(mContext, getString(R.string.tip_forward_kick));
                return;
            } else if (friend.getGroupStatus() == 2) {
                DialogHelper.tip(mContext, getString(R.string.tip_forward_disbanded));
                return;
            } else if ((friend.getGroupStatus() == 3)) {
                DialogHelper.tip(mContext, getString(R.string.tip_group_disable_by_service));
                return;
            }
        }

        mLoadFrame = new LoadFrame(ShareNearChatFriend.this);
        mLoadFrame.setSomething(getString(R.string.back_app, mShareBean.getAppName()), new LoadFrame.OnLoadFrameClickListener() {
            @Override
            public void cancelClick() {
                if (DeviceInfoUtil.isOppoRom()) {
                    // 调试发现OPPO手机被调起后当前界面不会自动回到后台，手动调一下
                    moveTaskToBack(true);
                }

                finish();
            }

            @Override
            public void confirmClick() {
                startActivity(new Intent(ShareNearChatFriend.this, MainActivity.class));
                finish();
            }
        });
        mLoadFrame.show();

        mShareChatMessage = new ChatMessage();
        mShareChatMessage.setType(Constants.TYPE_SHARE_LINK);
        mShareChatMessage.setFromUserId(coreManager.getSelf().getUserId());
        mShareChatMessage.setFromUserName(coreManager.getSelf().getNickName());
        mShareChatMessage.setToUserId(friend.getUserId());
        mShareChatMessage.setContent(getString(R.string.msg_link));
        mShareChatMessage.setObjectId(mShareContent);
        mShareChatMessage.setPacketId(ToolUtils.getUUID());
        mShareChatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
        ChatMessageDao.getInstance().saveNewSingleChatMessage(coreManager.getSelf().getUserId(), friend.getUserId(), mShareChatMessage);
        if (friend.getRoomFlag() == 1) {
            ImHelper.sendMucChatMessage(friend.getUserId(), mShareChatMessage);
        } else {
            ImHelper.sendChatMessage(friend.getUserId(), mShareChatMessage);
        }
    }

    class ClickListener implements OnClickListener {
        private Friend friend;

        ClickListener(Friend friend) {
            this.friend = friend;
        }

        @Override
        public void onClick(View v) {
            menuWindow.dismiss();
            switch (v.getId()) {
                case R.id.btn_send:
                    verifyAccount(3, friend);
                    break;
                case R.id.btn_cancle:
                    break;
                default:
                    break;
            }
        }
    }

    private void backtrack(String error) {
        Intent intent1 = new Intent();
        intent1.putExtra("tig_error", error);
        setResult(RESULT_OK, intent1);
        finish();
    }

    class MessageRecentlyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (mFriends != null) {
                return mFriends.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mFriends != null) {
                return mFriends.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            if (mFriends != null) {
                return position;
            }
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(ShareNearChatFriend.this, R.layout.item_recently_contacts, null);
                holder = new ViewHolder();
                holder.mIvHead = convertView.findViewById(R.id.iv_recently_contacts_head);
                holder.mTvName = convertView.findViewById(R.id.tv_recently_contacts_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Friend friend = mFriends.get(position);
            holder.mIvHead.fillData(friend);
            holder.mTvName.setText(TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName());
            return convertView;
        }
    }

    class ViewHolder {
        MessageAvatar mIvHead;
        TextView mTvName;
    }
}
