package com.iimm.miliao.course;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.iimm.miliao.MyApplication;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.message.MucRoom;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.RoomMemberDao;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.message.InstantMessageConfirm;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.view.MessageAvatar;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class SelectFriendsActivity extends BaseActivity {
    private ListView mLvRecentlyMessage;
    private List<Friend> friends;
    private InstantMessageConfirm menuWindow;
    private String mLoginUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messageinstant);
        initActionBar();
        mLoginUserId = coreManager.getSelf().getUserId();
        loadData();
        initView();
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
        tvTitle.setText(getString(R.string.most_recent_contact));
    }

    private void loadData() {
        friends = FriendDao.getInstance().getNearlyFriendMsg(mLoginUserId);
        List<Friend> disableList = new ArrayList<>();
        for (int i = 0; i < friends.size(); i++) {
            if (friends.get(i).getUserId().equals(Constants.ID_NEW_FRIEND_MESSAGE)
                    || friends.get(i).getIsDevice() == 1) {
                disableList.add(friends.get(i));
            }
        }
        friends.removeAll(disableList);
    }

    private void initView() {
        findViewById(R.id.tv_create_newmessage).setVisibility(View.GONE);
        mLvRecentlyMessage = (ListView) findViewById(R.id.lv_recently_message);
        mLvRecentlyMessage.setAdapter(new MessageRecentlyAdapter());
        mLvRecentlyMessage.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                Friend friend = friends.get(position);
                showPopuWindow(view, friend);
            }
        });
    }

    private void back(Friend friend) {
        menuWindow.dismiss();

        if (Constants.IS_SENDONG_COURSE_NOW) {
            DialogHelper.tip(SelectFriendsActivity.this, getString(R.string.send_course_wait));
            return;
        }

        if (friend.getRoomFlag() != 0) {// 群组
            isSupportSend(friend);
            return;
        }
        sendStep(friend);

    }

    private void sendStep(Friend friend) {
        Constants.IS_SENDONG_COURSE_NOW = true;

        Intent intent = new Intent();
        intent.putExtra("toUserId", friend.getUserId());
        intent.putExtra("isGroup", (friend.getRoomFlag() != 0));
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 获取自己在该群组的信息(职位、昵称、禁言时间等)以及群属性
     */
    private void isSupportSend(final Friend friend) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", friend.getRoomId());

        HttpUtils.get().url(coreManager.getConfig().ROOM_GET_ROOM)
                .params(params)
                .build()
                .execute(new BaseCallback<MucRoom>(MucRoom.class) {

                             @Override
                             public void onResponse(ObjectResult<MucRoom> result) {// 数据结果与room/get接口一样，只是服务端没有返回群成员列表的数据
                                 if (result.getResultCode() == 1 && result.getData() != null) {
                                     final MucRoom mucRoom = result.getData();
                                     if (mucRoom.getMember() == null) {// 被踢出该群组
                                         FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, mucRoom.getJid(), 1);// 更新本地群组状态
                                         DialogHelper.tip(SelectFriendsActivity.this, getString(R.string.tip_forward_kick));
                                     } else {// 正常状态
                                         int role = mucRoom.getMember().getRole();
                                         // 更新禁言状态
                                         FriendDao.getInstance().updateRoomTalkTime(mLoginUserId, mucRoom.getJid(), mucRoom.getMember().getTalkTime());

                                         // 更新部分群属性
                                         MyApplication.getInstance().saveGroupPartStatus(mucRoom.getJid(), mucRoom.getShowRead(),
                                                 mucRoom.getAllowSendCard(), mucRoom.getAllowConference(),
                                                 mucRoom.getAllowSpeakCourse(), mucRoom.getTalkTime());

                                         // 更新个人职位
                                         RoomMemberDao.getInstance().updateRoomMemberRole(mucRoom.getId(), mLoginUserId, role);

                                         if (role == 1 || role == 2) {// 群组或管理员 直接转发出去
                                             sendStep(friend);
                                         } else {
                                             if (mucRoom.getTalkTime() > 0) {// 全体禁言
                                                 DialogHelper.tip(SelectFriendsActivity.this, getString(R.string.tip_now_ban_all));
                                             } else if (mucRoom.getMember().getTalkTime() > System.currentTimeMillis() / 1000) {// 禁言
                                                 DialogHelper.tip(SelectFriendsActivity.this, getString(R.string.tip_forward_ban));
                                             } else if (mucRoom.getAllowSpeakCourse() == 0) {// 禁止发送讲课
                                                 DialogHelper.tip(SelectFriendsActivity.this, getString(R.string.tip_disabled_send_cource));
                                             } else {
                                                 sendStep(friend);
                                             }
                                         }
                                     }
                                 } else {// 群组已解散
                                     FriendDao.getInstance().updateFriendGroupStatus(mLoginUserId, friend.getUserId(), 2);// 更新本地群组状态
                                     DialogHelper.tip(SelectFriendsActivity.this, getString(R.string.tip_forward_disbanded));
                                 }
                             }

                             @Override
                             public void onError(Call call, Exception e) {
                                 ToastUtil.showNetError(mContext);
                             }
                         }
                );
    }

    private void showPopuWindow(View view, Friend friend) {
        menuWindow = new InstantMessageConfirm(SelectFriendsActivity.this, new ClickListener(friend), friend);
        menuWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    /**
     * 事件的监听
     */
    class ClickListener implements OnClickListener {
        private Friend friend;

        public ClickListener(Friend friend) {
            this.friend = friend;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_send:
                    // 发送
                    back(friend);
                    break;
                case R.id.btn_cancle:
                    // 取消
                    menuWindow.dismiss();
                    break;
                default:
                    break;
            }
        }
    }

    class MessageRecentlyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (friends != null) {
                return friends.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (friends != null) {
                return friends.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            if (friends != null) {
                return position;
            }
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(SelectFriendsActivity.this, R.layout.item_course_contacts, null);
                holder = new ViewHolder();
                holder.mIvHead = (MessageAvatar) convertView.findViewById(R.id.iv_course_contacts_head);
                holder.mTvName = (TextView) convertView.findViewById(R.id.tv_recently_contacts_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Friend friend = friends.get(position);
            holder.mIvHead.fillData(friend);
            holder.mTvName.setText(TextUtils.isEmpty(friend.getRemarkName())
                    ? friend.getNickName() : friend.getRemarkName());
            return convertView;
        }
    }

    class ViewHolder {
        MessageAvatar mIvHead;
        TextView mTvName;
    }
}
