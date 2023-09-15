package com.iimm.miliao.ui.groupchat;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.AppConstant;
import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.Area;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.message.ChatMessage;
import com.iimm.miliao.bean.message.MucRoom;
import com.iimm.miliao.broadcast.MsgBroadcast;
import com.iimm.miliao.broadcast.MucgroupUpdateUtil;
import com.iimm.miliao.call.MessageEventInitiateMeeting;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.ChatMessageDao;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.sortlist.BaseComparator;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.sortlist.SideBar;
import com.iimm.miliao.sortlist.SortHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.dialog.TowInputDialogView;
import com.iimm.miliao.ui.message.MucChatActivity;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.CharUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DisplayUtil;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.TimeUtils;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ViewHolder;
import com.iimm.miliao.view.HorizontalListView;
import com.iimm.miliao.view.TipDialog;
import com.iimm.miliao.xmpp.util.ImHelper;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * 选择联系人 发起群聊
 */
public class SelectContactsActivity extends BaseActivity {
    private EditText mEditText;
    private boolean isSearch;
    private SideBar mSideBar;
    private TextView mTextDialog;
    private ListView mListView;
    private ListViewAdapter mAdapter;
    private List<Friend> mFriendList;
    private List<BaseSortModel<Friend>> mSortFriends;
    private List<BaseSortModel<Friend>> mSearchSortFriends;
    private BaseComparator<Friend> mBaseComparator;
    private HorizontalListView mHorizontalListView;
    private HorListViewAdapter mHorAdapter;
    private List<String> mSelectPositions;
    private Button mOkBtn;
    private String mLoginUserId;
    // 快速发起会议
    private boolean mQuicklyInitiateMeeting;
    private boolean isAudio;
    // 是否是通过单人聊天快速创建的群组
    private boolean mQuicklyCreate;
    // 快速建群时聊天对象的id与备注名/昵称
    private String mQuicklyId;
    private String mQuicklyName;
    private TowInputDialogView towInputDialogView;

    public static void startQuicklyInitiateMeeting(Context ctx) {
        Dialog bottomDialog = new Dialog(ctx, R.style.BottomDialog);
        View contentView = LayoutInflater.from(ctx).inflate(R.layout.dialog_select_media, null);
        TextView textView = (TextView) contentView.findViewById(R.id.dialog_select_voice);
        textView.setText(InternationalizationHelper.getString("JX_Meeting"));
        TextView textView_02 = (TextView) contentView.findViewById(R.id.dialog_select_video);
        textView_02.setText(InternationalizationHelper.getString("JXSettingVC_VideoMeeting"));
        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = ctx.getResources().getDisplayMetrics().widthPixels;
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.show();
        contentView.findViewById(R.id.dialog_select_cancel).setOnClickListener(v -> {
            bottomDialog.cancel();
        });
        contentView.findViewById(R.id.dialog_select_voice_ll).setOnClickListener(v -> {
            bottomDialog.cancel();
            startQuicklyInitiateMeeting(ctx, true);
        });
        contentView.findViewById(R.id.dialog_select_video_ll).setOnClickListener(v -> {
            bottomDialog.cancel();
            startQuicklyInitiateMeeting(ctx, false);
        });

    }

    public static void startQuicklyInitiateMeeting(Context ctx, boolean isAudio) {
        Intent quicklyInitiateMeetingIntent = new Intent(ctx, SelectContactsActivity.class);
        quicklyInitiateMeetingIntent.putExtra("QuicklyInitiateMeeting", true);
        quicklyInitiateMeetingIntent.putExtra("isAudio", isAudio);
        ctx.startActivity(quicklyInitiateMeetingIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);
        if (getIntent() != null) {
            mQuicklyInitiateMeeting = getIntent().getBooleanExtra("QuicklyInitiateMeeting", false);
            isAudio = getIntent().getBooleanExtra("isAudio", false);

            mQuicklyCreate = getIntent().getBooleanExtra("QuicklyCreateGroup", false);
            mQuicklyId = getIntent().getStringExtra("ChatObjectId");
            mQuicklyName = getIntent().getStringExtra("ChatObjectName");
        }
        mLoginUserId = coreManager.getSelf().getUserId();

        mFriendList = new ArrayList<>();
        mSortFriends = new ArrayList<>();
        mSearchSortFriends = new ArrayList<>();
        mBaseComparator = new BaseComparator<>();
        mAdapter = new ListViewAdapter();

        mSelectPositions = new ArrayList<>();
        mHorAdapter = new HorListViewAdapter();

        initActionBar();
        initView();

        if (!mQuicklyInitiateMeeting && coreManager.getLimit().cannotCreateGroup()) {
            Reporter.unreachable();
            TipDialog tipDialog = new TipDialog(this);
            tipDialog.setTip(getString(R.string.tip_not_allow_create_room));
            tipDialog.setOnDismissListener(dialog -> {
                finish();
            });
            tipDialog.show();
        }

    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        if (mQuicklyInitiateMeeting) {
            tvTitle.setText(getString(R.string.select_contacts));
        } else {
            tvTitle.setText(InternationalizationHelper.getString("SELECT_GROUP_MEMBERS"));
        }
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setAdapter(mAdapter);
        mHorizontalListView = (HorizontalListView) findViewById(R.id.horizontal_list_view);
        mHorizontalListView.setAdapter(mHorAdapter);
        mOkBtn = (Button) findViewById(R.id.ok_btn);
        // mOkBtn.setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());

        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mSideBar.setVisibility(View.VISIBLE);
        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar.setTextView(mTextDialog);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = mAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }
            }
        });

        /**
         * 创建群组邀请好友搜索功能
         */
        mEditText = (EditText) findViewById(R.id.search_et);
        mEditText.setHint(InternationalizationHelper.getString("JX_Seach"));
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                isSearch = true;
                mSearchSortFriends.clear();
                String str = mEditText.getText().toString();
                if (TextUtils.isEmpty(str)) {
                    isSearch = false;
                    mAdapter.setData(mSortFriends);
                    return;
                }
                for (int i = 0; i < mSortFriends.size(); i++) {
                  /*  String name = !TextUtils.isEmpty(mSortFriends.get(i).getBean().getRemarkName()) ?
                            mSortFriends.get(i).getBean().getRemarkName() : mSortFriends.get(i).getBean().getNickName();*/
                    String name = mSortFriends.get(i).getBean().getRemarkName() + mSortFriends.get(i).getBean().getNickName();
                    if (name.contains(str)) {
                        // 符合搜索条件的好友
                        mSearchSortFriends.add((mSortFriends.get(i)));
                    }
                }
                mAdapter.setData(mSearchSortFriends);
            }
        });

        mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() + ""));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Friend friend;
                if (isSearch) {
                    friend = mSearchSortFriends.get(position).bean;
                } else {
                    friend = mSortFriends.get(position).bean;
                }

                if (mQuicklyCreate) {
                    if (friend.getUserId().equals(mLoginUserId)) {
                        ToastUtil.showToast(SelectContactsActivity.this, getString(R.string.tip_cannot_remove_self));
                        return;
                    } else if (friend.getUserId().equals(mQuicklyId)) {
                        ToastUtil.showToast(SelectContactsActivity.this, getString(R.string.tip_quickly_group_cannot_remove) + mQuicklyName);
                        return;
                    }
                }

                for (int i = 0; i < mSortFriends.size(); i++) {
                    if (mSortFriends.get(i).getBean().getUserId().equals(friend.getUserId())) {
                        if (friend.getStatus() != 100) {
                            friend.setStatus(100);
                            mSortFriends.get(i).getBean().setStatus(100);
                            addSelect(friend.getUserId());
                        } else {
                            friend.setStatus(101);
                            mSortFriends.get(i).getBean().setStatus(101);
                            removeSelect(friend.getUserId());
                        }

                        if (isSearch) {
                            mAdapter.setData(mSearchSortFriends);
                        } else {
                            mAdapter.setData(mSortFriends);
                        }
                    }
                }
            }
        });

        mHorizontalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                for (int i = 0; i < mSortFriends.size(); i++) {
                    if (mSortFriends.get(i).getBean().getUserId().equals(mSelectPositions.get(position))) {
                        mSortFriends.get(i).getBean().setStatus(101);
                        mAdapter.setData(mSortFriends);
                    }
                }
                mSelectPositions.remove(position);
                mHorAdapter.notifyDataSetInvalidated();
                mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() + ""));
            }
        });

        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQuicklyInitiateMeeting) {// 快速发起音视频会议
                    showSelectMeetingTypeDialog();
                    return;
                }
                ImHelper.checkXmppAuthenticated();
                if (mQuicklyCreate) {
                    // 因为前面已经为mSelectPositions增加了一个虚线框,So
                    if (mSelectPositions.size() <= 0) {
                        ToastUtil.showToast(mContext, getString(R.string.tip_create_group_at_lease_one_friend));
                        return;
                    }
                    String sc = coreManager.getSelf().getNickName() + "、" + mQuicklyName + "、";
                    for (int i = 0; i < mSelectPositions.size(); i++) {
                        String name = "";
                        for (int i1 = 0; i1 < mFriendList.size(); i1++) {
                            if (mFriendList.get(i1).getUserId().equals(mSelectPositions.get(i))) {
                                name = !TextUtils.isEmpty(mFriendList.get(i1).getRemarkName()) ? mFriendList.get(i1).getRemarkName() : mFriendList.get(i1).getNickName();
                            }
                        }
                        if (i == mSelectPositions.size() - 1) {
                            sc += name;
                        } else {
                            sc += name + "、";
                        }
                    }
                    createGroupChat(sc, "", 0, 1, 0, 1, 1);
                } else {
//                    showCreateGroupChatDialog();
                    if (mSelectPositions.size() < 2) {
                        ToastUtil.showToast(mContext, getString(R.string.tip_create_group_at_lease_two_friend));
                        return;
                    }
                    String sc = coreManager.getSelf().getNickName() + "、";
                    int size = 0;
                    if (mSelectPositions.size() > 2) {
                        size = 2;
                    } else {
                        size = mSelectPositions.size();
                    }
                    for (int i = 0; i < size; i++) {
                        String name = "";
                        for (int i1 = 0; i1 < mFriendList.size(); i1++) {
                            if (mFriendList.get(i1).getUserId().equals(mSelectPositions.get(i))) {
                                name = !TextUtils.isEmpty(mFriendList.get(i1).getRemarkName()) ? mFriendList.get(i1).getRemarkName() : mFriendList.get(i1).getNickName();
                            }
                        }
                        if (i == size - 1) {
                            sc += name;
                        } else {
                            sc += name + "、";
                        }
                    }
                    createGroupChat(sc, "暂无描述", 0, 1, 0, 1, 1);
                }
            }
        });

        loadData();
    }

    private void loadData() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        AsyncUtils.doAsync(this, e -> {
            Reporter.post("加载数据失败，", e);
            AsyncUtils.runOnUiThread(this, ctx -> {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(ctx, R.string.data_exception);
            });
        }, c -> {
            final List<Friend> friends = FriendDao.getInstance().getFriendsGroupChat(mLoginUserId);
            if (mQuicklyCreate) {
                Friend friend = new Friend();
                friend.setUserId(mLoginUserId);
                friend.setNickName(coreManager.getSelf().getNickName());
                friends.add(0, friend);
            }
            Map<String, Integer> existMap = new HashMap<>();
            List<BaseSortModel<Friend>> sortedList = SortHelper.toSortedModelList(friends, existMap, Friend::getShowName);
            c.uiThread(r -> {
                DialogHelper.dismissProgressDialog();
                mSideBar.setExistMap(existMap);
                mFriendList = friends;
                mSortFriends = sortedList;
                mAdapter.setData(sortedList);
            });
        });
    }

    private void addSelect(String userId) {
        mSelectPositions.add(userId);
        mHorAdapter.notifyDataSetInvalidated();
        mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() + ""));
    }

    private void removeSelect(String userId) {
        for (int i = 0; i < mSelectPositions.size(); i++) {
            if (mSelectPositions.get(i).equals(userId)) {
                mSelectPositions.remove(i);
            }
        }
        mHorAdapter.notifyDataSetInvalidated();
        mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() + ""));
    }

    private void showCreateGroupChatDialog() {
        towInputDialogView = DialogHelper.showTowInputDialogAndReturnDialog(this,
                InternationalizationHelper.getString("CREATE_ROOMS"),
                InternationalizationHelper.getString("JX_InputRoomName"),
                InternationalizationHelper.getString("JXAlert_InputSomething"),
                new TowInputDialogView.onSureClickLinsenter() {
                    @Override
                    public void onClick(EditText roomNameEdit, EditText roomDescEdit, int isRead, int isLook, int isNeedVerify, int isShowMember, int isAllowSendCard) {
                        String roomName = roomNameEdit.getText().toString().trim();
                        if (TextUtils.isEmpty(roomName)) {
                            Toast.makeText(SelectContactsActivity.this, getString(R.string.room_name_empty_error), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String roomDesc = roomDescEdit.getText().toString();
                        if (TextUtils.isEmpty(roomDesc)) {
                            Toast.makeText(SelectContactsActivity.this, getString(R.string.room_des_empty_error), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int length = 0;
                        for (int i = 0; i < roomName.length(); i++) {
                            String substring = roomName.substring(i, i + 1);
                            if (CharUtils.isChinese(substring)) {  // 中文占两个字符
                                length += 2;
                            } else {
                                length += 1;
                            }
                        }
                        if (length > 20) {
                            Toast.makeText(SelectContactsActivity.this, R.string.tip_group_name_too_long, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int length2 = 0;
                        for (int i = 0; i < roomDesc.length(); i++) {
                            String substring = roomDesc.substring(i, i + 1);
                            if (CharUtils.isChinese(substring)) {
                                length2 += 2;
                            } else {
                                length2 += 1;
                            }
                        }
                        if (length2 > 100) {
                            Toast.makeText(SelectContactsActivity.this, R.string.tip_group_description_too_long, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        createGroupChat(roomName, roomDesc, isRead, isLook, isNeedVerify, isShowMember, isAllowSendCard);

                        if (towInputDialogView != null) {
                            towInputDialogView.dismiss();
                        }
                    }
                });

        // 设置输入监听
       /* ToastUtil.addEditTextNumChanged(SelectContactsActivity.this, dialogView.getE1(), 20);
        ToastUtil.addEditTextNumChanged(SelectContactsActivity.this, dialogView.getE2(), 100);*/
    }

    private void createGroupChat(final String roomName, final String roomDesc, int isRead,
                                 int isLook, int isNeedVerify, int isShowMember, int isAllowSendCard) {

        if (!ImHelper.canCreateGroup(coreManager)) {
            MyApplication.applicationHandler.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast(MyApplication.getContext(), "当前账号不允许创建群组");
                }
            });
            return;
        }
        final String roomJid = ImHelper.createMucRoom(roomName);
        if (TextUtils.isEmpty(roomJid)) {
            ToastUtil.showToast(mContext, getString(R.string.create_room_failed));
            return;
        }
        MyApplication.mRoomKeyLastCreate = roomJid;
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("jid", roomJid);
        params.put("name", roomName);
        params.put("desc", roomDesc);
        params.put("countryId", String.valueOf(Area.getDefaultCountyId()));

        params.put("showRead", isRead + "");
        // 显示已读人数
        PreferenceUtils.putBoolean(mContext, Constants.IS_SHOW_READ + roomJid, isRead == 1);
        // 是否公开
        params.put("isLook", isLook + "");
        // 是否开启进群验证
        params.put("isNeedVerify", isNeedVerify + "");
        // 其他群管理
        params.put("showMember", isShowMember + "");
        params.put("allowSendCard", isAllowSendCard + "");

        params.put("allowInviteFriend", "1");
        params.put("allowUploadFile", "1");
        params.put("allowConference", "1");
        params.put("allowSpeakCourse", "1");

        PreferenceUtils.putBoolean(mContext, Constants.IS_SEND_CARD + roomJid, isAllowSendCard == 1);

        Area area = Area.getDefaultProvince();
        if (area != null) {
            params.put("provinceId", String.valueOf(area.getId()));    // 省份Id
        }
        area = Area.getDefaultCity();
        if (area != null) {
            params.put("cityId", String.valueOf(area.getId()));            // 城市Id
            area = Area.getDefaultDistrict(area.getId());
            if (area != null) {
                params.put("areaId", String.valueOf(area.getId()));        // 城市Id
            }
        }

        double latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
        double longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();
        if (latitude != 0)
            params.put("latitude", String.valueOf(latitude));
        if (longitude != 0)
            params.put("longitude", String.valueOf(longitude));

        // 邀请好友
        List<String> inviteUsers = new ArrayList<>();
        for (int i = 0; i < mSelectPositions.size(); i++) {
            String userId = mSelectPositions.get(i);
            inviteUsers.add(userId);
        }
        if (mQuicklyCreate) {
            inviteUsers.add(mQuicklyId);
        }
        params.put("text", JSON.toJSONString(inviteUsers));
        DialogHelper.showDefaulteMessageProgressDialog(this);
        mOkBtn.setEnabled(false);

        HttpUtils.get().url(coreManager.getConfig().ROOM_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<MucRoom>(MucRoom.class) {

                    @Override
                    public void onResponse(ObjectResult<MucRoom> result) {
                        mOkBtn.setEnabled(true);
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            if (mQuicklyCreate) {
                                sendBroadcast(new Intent(com.iimm.miliao.broadcast.OtherBroadcast.QC_FINISH)); // 快速建群成功，发送广播关闭之前的单聊界面
                            }
                            createRoomSuccess(result.getData().getId(), roomJid, roomName, roomDesc);
                        } else {
                            MyApplication.mRoomKeyLastCreate = "compatible";// 还原回去
                            if (!TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(mContext, result.getResultMsg());
                            } else {
                                ToastUtil.showToast(mContext, R.string.tip_server_error);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        mOkBtn.setEnabled(true);
                        DialogHelper.dismissProgressDialog();
                        MyApplication.mRoomKeyLastCreate = "compatible";// 还原回去
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    // 创建成功的时候将会调用此方法，将房间也存为好友
    private void createRoomSuccess(String roomId, String roomJid, String roomName, String
            roomDesc) {
        Friend friend = new Friend();
        friend.setOwnerId(mLoginUserId);
        friend.setUserId(roomJid);
        friend.setNickName(roomName);
        friend.setDescription(roomDesc);
        friend.setRoomFlag(1);
        friend.setRoomId(roomId);
        friend.setRoomCreateUserId(mLoginUserId);
        // timeSend作为取群聊离线消息的标志，所以要在这里设置一个初始值
        friend.setTimeSend(TimeUtils.time_current_time());
        friend.setStatus(Constants.STATUS_FRIEND);
        FriendDao.getInstance().createOrUpdateFriend(friend);

        // 更新群组
        MucgroupUpdateUtil.broadcastUpdateUi(this);

        // 本地发送一条消息至该群 否则未邀请其他人时在消息列表不会显示
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(Constants.TYPE_TIP);
        chatMessage.setFromUserId(mLoginUserId);
        chatMessage.setFromUserName(coreManager.getSelf().getNickName());
        chatMessage.setToUserId(roomJid);
        chatMessage.setContent(InternationalizationHelper.getString("NEW_FRIEND_CHAT"));
        chatMessage.setPacketId(coreManager.getSelf().getNickName());
        chatMessage.setDoubleTimeSend(TimeUtils.time_current_time_double());
        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, roomJid, chatMessage)) {
            // 更新聊天界面
            MsgBroadcast.broadcastMsgUiUpdate(SelectContactsActivity.this);
        }

        // 邀请好友
        String[] noticeFriendList = new String[mSelectPositions.size()];
        List<String> inviteUsers = new ArrayList<>();

        // 邀请好友
        for (int i = 0; i < mSelectPositions.size(); i++) {
            String userId = mSelectPositions.get(i);
            inviteUsers.add(userId);
        }
        if (mQuicklyCreate) {
            inviteUsers.add(mQuicklyId);
        }
        inviteFriend(JSON.toJSONString(inviteUsers), roomId, roomJid, roomName, noticeFriendList);
    }

    /**
     * 邀请好友
     */
    private void inviteFriend(String text, String roomId, final String roomJid,
                              final String roomName, final String[] noticeFriendList) {
        if (mSelectPositions.size() <= 0) {
            // 进入群聊界面，结束当前的界面
            Intent intent = new Intent(SelectContactsActivity.this, MucChatActivity.class);
            intent.putExtra(AppConstant.EXTRA_USER_ID, roomJid);
            intent.putExtra(AppConstant.EXTRA_NICK_NAME, roomName);
            intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
            startActivity(intent);
            finish();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", roomId);
        params.put("text", text);

        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        setResult(RESULT_OK);
                        // 进入群聊界面，结束当前的界面
                        Intent intent = new Intent(SelectContactsActivity.this, MucChatActivity.class);
                        intent.putExtra(AppConstant.EXTRA_USER_ID, roomJid);
                        intent.putExtra(AppConstant.EXTRA_NICK_NAME, roomName);
                        intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
                        intent.putExtra(Constants.GROUP_JOIN_NOTICE, noticeFriendList);
                        intent.putExtra(AppConstant.EXTRA_CREATE_GROUP, true);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private void showSelectMeetingTypeDialog() {
        if (mSelectPositions.size() == 0) {
            DialogHelper.tip(this, getString(R.string.tip_select_at_lease_one_member));
            return;
        }
        EventBus.getDefault().post(new MessageEventInitiateMeeting(isAudio, mSelectPositions));
    }

    private class ListViewAdapter extends BaseAdapter implements SectionIndexer {
        List<BaseSortModel<Friend>> mSortFriends;

        public ListViewAdapter() {
            mSortFriends = new ArrayList<>();
        }

        public void setData(List<BaseSortModel<Friend>> sortFriends) {
            mSortFriends = sortFriends;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mSortFriends.size();
        }

        @Override
        public Object getItem(int position) {
            return mSortFriends.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_select_contacts, parent, false);
            }
            TextView catagoryTitleTv = ViewHolder.get(convertView, R.id.catagory_title);
            CheckBox checkBox = ViewHolder.get(convertView, R.id.check_box);
            ImageView avatarImg = ViewHolder.get(convertView, R.id.avatar_img);
            TextView userNameTv = ViewHolder.get(convertView, R.id.user_name_tv);
            TextView userRemarkNameTv = ViewHolder.get(convertView, R.id.user_remark_name_tv);

            // 根据position获取分类的首字母的Char ascii值
            int section = getSectionForPosition(position);
            // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (position == getPositionForSection(section)) {
                catagoryTitleTv.setVisibility(View.VISIBLE);
                catagoryTitleTv.setText(mSortFriends.get(position).getFirstLetter());
            } else {
                catagoryTitleTv.setVisibility(View.GONE);
            }
            Friend friend = mSortFriends.get(position).getBean();
            if (friend != null) {
                AvatarHelper.getInstance().displayAvatar(friend.getUserId(), avatarImg, true);
//                userNameTv.setText(TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName());
                userNameTv.setText(friend.getNickName());

                if (TextUtils.isEmpty(friend.getRemarkName())) {
                    userRemarkNameTv.setVisibility(View.GONE);
                } else {
                    userRemarkNameTv.setVisibility(View.VISIBLE);
                    userRemarkNameTv.setText("备注: " + friend.getRemarkName());
                }

                checkBox.setChecked(false);
                if (friend.getStatus() == 100) {
                    checkBox.setChecked(true);
                } else {
                    checkBox.setChecked(false);
                }

                // 快速建群，添加两行item项，默认选中且不可点击
                if (mQuicklyCreate) {
                    if (friend.getUserId().equals(mLoginUserId) || friend.getUserId().equals(mQuicklyId)) {
                        checkBox.setChecked(true);
                    }
                }
            }
            return convertView;
        }

        @Override
        public Object[] getSections() {
            return null;
        }

        @Override
        public int getPositionForSection(int section) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = mSortFriends.get(i).getFirstLetter();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getSectionForPosition(int position) {
            return mSortFriends.get(position).getFirstLetter().charAt(0);
        }
    }

    private class HorListViewAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mSelectPositions.size();
        }

        @Override
        public Object getItem(int position) {
            return mSelectPositions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new RoundedImageView(mContext);
                int size = DisplayUtil.dip2px(mContext, 36);
                ((RoundedImageView) convertView).setOval(false);
                ((RoundedImageView) convertView).setCornerRadius(Constants.AVATAR_CORNER_RADIUS);
                AbsListView.LayoutParams param = new AbsListView.LayoutParams(size, size);
                convertView.setLayoutParams(param);
            }
            ImageView imageView = (ImageView) convertView;
            String selectPosition = mSelectPositions.get(position);
            AvatarHelper.getInstance().displayAvatar(selectPosition, imageView, true);
            return convertView;
        }
    }
}
