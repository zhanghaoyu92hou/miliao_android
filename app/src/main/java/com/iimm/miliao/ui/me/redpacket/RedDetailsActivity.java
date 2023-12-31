package com.iimm.miliao.ui.me.redpacket;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gyf.immersionbar.ImmersionBar;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.RoomMember;
import com.iimm.miliao.bean.redpacket.OpenRedpacket;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.RoomMemberDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * modify by zq
 * 单聊 普通红包、口令红包自己都不可领取
 * 群组 手气红包、口令红包自己可领取，普通红包不可领取
 */
public class RedDetailsActivity extends BaseActivity implements View.OnClickListener {
    LayoutInflater inflater;
    DecimalFormat df = new DecimalFormat("######0.00");
    private ImageView red_head_iv;
    private TextView red_nickname_tv;
    private TextView red_words_tv;
    private TextView red_money_tv;
    private TextView red_money_bit_tv;
    private TextView red_reply_tv;
    private TextView red_resultmsg_tv;
    private ListView red_details_lsv;
    private RelativeLayout get_money_bit_rl;
    private RedAdapter mRedAdapter;
    private OpenRedpacket openRedpacket;
    private OpenRedpacket.PacketEntity packetEntity;
    private List<OpenRedpacket.ListEntity> list;
    private int redAction;  // 标记是抢到红包还是查看了红包
    private int timeOut;    // 标记红包是否已过时
    private boolean isGroup;// 是否为群组
    private String mToUserId; // userId || 群组jid
    private Friend mFriend; // 通过该mFriend，获取备注名、获取群成员表显示群内昵称
    private String resultMsg, redMsg, msg;
    private Map<String, String> mGroupNickNameMap = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redpacket_details);
        Bundle bundle = getIntent().getExtras();
        openRedpacket = (OpenRedpacket) bundle.getSerializable("openRedpacket");
        redAction = bundle.getInt("redAction");
        timeOut = bundle.getInt("timeOut");
        isGroup = bundle.getBoolean("isGroup", false);
        mToUserId = bundle.getString("mToUserId");
        msg = bundle.getString("msg");
        list = openRedpacket.getList();
        packetEntity = openRedpacket.getPacket();
        inflater = LayoutInflater.from(this);
        initView();
        showData();
    }

    private void initView() {
        getSupportActionBar().hide();

        View view = findViewById(R.id.battery_bar_v);
        ImmersionBar.with(this).statusBarView(view)
                .init();
        red_head_iv = (ImageView) findViewById(R.id.red_head_iv);
        red_nickname_tv = (TextView) findViewById(R.id.red_nickname_tv);
        red_words_tv = (TextView) findViewById(R.id.red_words_tv);
        red_money_tv = (TextView) findViewById(R.id.get_money_tv);
        red_money_bit_tv = (TextView) findViewById(R.id.get_money_bit_tv);
        red_reply_tv = (TextView) findViewById(R.id.reply_red_tv);
        get_money_bit_rl = findViewById(R.id.get_money_bit_rl);

        red_resultmsg_tv = (TextView) findViewById(R.id.red_resultmsg_tv);
        red_details_lsv = (ListView) findViewById(R.id.red_details_lsv);

        mFriend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), mToUserId);
        if (isGroup && mFriend != null) {// 群组红包 获取群内昵称 之后显示
            List<RoomMember> mRoomMemberList = RoomMemberDao.getInstance().getRoomMember(mFriend.getRoomId());
            if (mRoomMemberList != null && mRoomMemberList.size() > 0) {
                for (int i = 0; i < mRoomMemberList.size(); i++) {
                    RoomMember mRoomMember = mRoomMemberList.get(i);
                    mGroupNickNameMap.put(mRoomMember.getUserId(), mRoomMember.getUserName());
                }
            }
        }

        red_reply_tv.setOnClickListener(this);
        findViewById(R.id.red_back_tv).setOnClickListener(this);
        findViewById(R.id.get_redlist_tv).setOnClickListener(this);
    }

    private void showData() {
        if (list == null) {
            list = new ArrayList<>();
        }
        AvatarHelper.getInstance().displayAvatar(packetEntity.getUserName(), packetEntity.getUserId(), red_head_iv, true);
        Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), packetEntity.getUserId());
        String name = "";
        if (friend != null) {
            name = TextUtils.isEmpty(friend.getRemarkName()) ? packetEntity.getUserName() : friend.getRemarkName();
        } else {
            name = packetEntity.getUserName();
        }
        red_nickname_tv.setText(getString(R.string.someone_s_red_packet_place_holder, name));
        red_words_tv.setText(packetEntity.getGreetings());

        boolean isReceivedSelf = false;
        for (OpenRedpacket.ListEntity entity : list) {
            if (entity.getUserId().equals(coreManager.getSelf().getUserId())) {
                isReceivedSelf = true;
                red_money_tv.setText(df.format(entity.getMoney()));
                if (!TextUtils.isEmpty(df.format(entity.getMoney()))) {
//                    red_money_bit_tv.setText(R.string.rmb);
                    red_reply_tv.setText(TextUtils.isEmpty(entity.getReply()) ? getString(R.string.reply_red_thank) : entity.getReply());
                }
            }
        }

        if (!isReceivedSelf) {// 没有领取红包，隐藏回复按钮
            red_reply_tv.setVisibility(View.GONE);
            get_money_bit_rl.setVisibility(View.GONE);
        } else {
            red_reply_tv.setVisibility(View.VISIBLE);
            get_money_bit_rl.setVisibility(View.VISIBLE);
        }

        resultMsg = getString(R.string.red_packet_receipt_place_holder, list.size(), packetEntity.getCount(),
                df.format(packetEntity.getMoney() - packetEntity.getOver()),
                df.format(packetEntity.getMoney()));

        if (list.size() == packetEntity.getCount()) {
            redMsg = getString(R.string.red_packet_receipt_suffix_all);
        } else if (openRedpacket.getPacket() != null && !TextUtils.isEmpty(openRedpacket.getPacket().getToUserIds())) {
            redMsg = "," + msg;
        } else if (timeOut == 1) {
            redMsg = getString(R.string.red_packet_receipt_suffix_over);
        } else {
            redMsg = getString(R.string.red_packet_receipt_suffix_remain);
        }


        red_resultmsg_tv.setText(resultMsg + redMsg);
        mRedAdapter = new RedAdapter();
        mRedAdapter.preload();
        red_details_lsv.setAdapter(mRedAdapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_title_left || v.getId() == R.id.red_back_tv) {
            finish();
        } else if (v.getId() == R.id.get_redlist_tv) {
            Intent intent = new Intent(RedDetailsActivity.this, RedListActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.reply_red_tv) {
            DialogHelper.ChangeRoomNameDialog(this, getString(R.string.replay),
                    getString(R.string.reply_red_thank) + getString(R.string.input_most_length, 10), 1, 1, 10, v1 -> {
                        final String text = ((EditText) v1).getText().toString().trim();
                        if (TextUtils.isEmpty(text)) {
                            return;
                        }
                        replyRed(text);
                    });
        }
    }

    // 查看红包领取详情
    private void replyRed(String reply) {
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(mContext).accessToken);
        params.put("id", openRedpacket.getPacket().getId());
        params.put("reply", reply);

        HttpUtils.get().url(CoreManager.requireConfig(mContext).RENDPACKET_REPLY)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            for (int i = 0; i < openRedpacket.getList().size(); i++) {
                                if (openRedpacket.getList().get(i).getUserId().equals(coreManager.getSelf().getUserId())) {
                                    red_reply_tv.setText(reply);
                                    openRedpacket.getList().get(i).setReply(reply);
                                    mRedAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    private class RedAdapter extends BaseAdapter {
        View view;
        private String lucklyUserId;

        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            OpenRedpacket.ListEntity listEntity = list.get(position);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long lcc_time = Long.valueOf(listEntity.getTime());
            if (String.valueOf(lcc_time).length()==10){
                lcc_time = lcc_time * 1000L;
            }
            String StrTime = sdf.format(new Date(lcc_time));

            view = inflater.inflate(R.layout.reditem_layout, null);
            String name;
            if (isGroup) {
                if (mGroupNickNameMap.size() > 0 && mGroupNickNameMap.containsKey(listEntity.getUserId())) {
                    Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), listEntity.getUserId());
                    if (friend != null) {
                        name = TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName();
                    } else {
                        name = mGroupNickNameMap.get(listEntity.getUserId());
                    }
                } else {
                    Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), listEntity.getUserId());
                    if (friend != null) {
                        name = TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName();
                    } else {
                        name = listEntity.getUserName();
                    }
                }
            } else {
                if (listEntity.getUserId().equals(coreManager.getSelf().getUserId())) {// 自己领取了
                    name = listEntity.getUserName();
                } else {
                    if (mFriend != null) {
                        name = TextUtils.isEmpty(mFriend.getRemarkName()) ? mFriend.getNickName() : mFriend.getRemarkName();
                    } else {
                        name = listEntity.getUserName();
                    }
                }
            }
            // 手气红包 && 红包已领完 显示手气最佳
            if (openRedpacket.getPacket().getType() == 2
                    && (openRedpacket.getPacket().getCount() == openRedpacket.getList().size())
                    && TextUtils.equals(lucklyUserId, listEntity.getUserId())) {
                view.findViewById(R.id.best_lucky_ll).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.best_lucky_ll).setVisibility(View.GONE);
            }
            AvatarHelper.getInstance().displayAvatar(name, listEntity.getUserId(), (ImageView) view.findViewById(R.id.red_head_iv), true);
            ((TextView) view.findViewById(R.id.username_tv)).setText(name);
            ((TextView) view.findViewById(R.id.opentime_tv)).setText(StrTime);
            ((TextView) view.findViewById(R.id.money_tv)).setText(df.format(listEntity.getMoney()) + getString(R.string.rmb));
            if (!TextUtils.isEmpty(listEntity.getReply())) {
                ((TextView) view.findViewById(R.id.reply_tv)).setText(listEntity.getReply());
            }
            return view;
        }

        public void preload() {
            // 按时间排序，避免手气最佳固定在最上面，
            Collections.sort(list, (o1, o2) ->
                    // 时间大的排上面，
                    -Long.compare(o1.getTime(), o2.getTime())
            );
            if (openRedpacket.getPacket().getCount() == openRedpacket.getList().size()) {
                // 计算手气最佳，
                OpenRedpacket.ListEntity max = Collections.max(list, (o1, o2) -> {
                            // 计算出领取金额最大的用户，
                            int dMoney = Double.compare(o1.getMoney(), o2.getMoney());
                            // 如果存在领取金额一样的，取时间小的，
                            if (dMoney == 0) {
                                return -Long.compare(o1.getTime(), o2.getTime());
                            }
                            return dMoney;
                        }

                );
                lucklyUserId = max.getUserId();
            }
        }
    }
}
