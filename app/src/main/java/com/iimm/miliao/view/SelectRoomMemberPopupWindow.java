package com.iimm.miliao.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.RoomMember;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.SkinUtils;
import com.iimm.miliao.util.ViewHolder;
import com.iimm.miliao.view.circularImageView.CircularImageVIew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @群成员pop
 */
public class SelectRoomMemberPopupWindow extends PopupWindow {

    private View mMenuView;
    private EditText mEditText;
    private LinearLayout mEveryOne;
    private CircularImageVIew iv;

    private ListView lv;
    private SetAtMemberAdapter mSetAtMemberAdapter;
    private List<RoomMember> roomMember;

    private Map<String, String> mRemarksMap = new HashMap<>();
    private int mRole;

    private SendMember mSendMember;

    public SelectRoomMemberPopupWindow(FragmentActivity context, SendMember sendMember, List<RoomMember> roomMember, int role) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.pop_at_room_member, null);
        mMenuView.findViewById(R.id.select_rl).setBackgroundColor(SkinUtils.getSkin(context).getPrimaryColor());
        if (role < Constants.ROLE_MEMBER) {
            mMenuView.findViewById(R.id.rl_everyone).setVisibility(View.VISIBLE);
        }
        this.mSendMember = sendMember;

        this.roomMember = roomMember;
        this.mRole = role;
        List<Friend> mFriendList = FriendDao.getInstance().getAllFriends(CoreManager.requireSelf(context).getUserId());
        for (int i = 0; i < mFriendList.size(); i++) {
            if (!TextUtils.isEmpty(mFriendList.get(i).getRemarkName())) {// 针对该好友进行了备注
                mRemarksMap.put(mFriendList.get(i).getUserId(), mFriendList.get(i).getRemarkName());
            }
        }

        //设置SelectRoomMemberPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectRoomMemberPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.MATCH_PARENT);
        //设置SelectRoomMemberPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.MATCH_PARENT);
        //设置SelectRoomMemberPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectRoomMemberPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.Buttom_Popwindow);
        //实例化一个SelectRoomMemberPopupWindow颜色为白色
        ColorDrawable dw = new ColorDrawable(context.getResources().getColor(R.color.app_white));
        //设置SelectRoomMemberPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);

        initView(context);
    }

    private void initView(Context context) {
        List<String> urlS = new ArrayList<>();
        ImageView ivS = (ImageView) mMenuView.findViewById(R.id.title_iv_back);
        ivS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mEveryOne = (LinearLayout) mMenuView.findViewById(R.id.everybody);
        iv = (CircularImageVIew) mMenuView.findViewById(R.id.everyone_iv);
        if (roomMember.size() > 0) {
            if (roomMember.size() > 5) {
                // 群组头像最多为5人组合
                for (int i = 0; i < 5; i++) {
                    String avatarUrl = AvatarHelper.getAvatarUrl(roomMember.get(i).getUserId(), true);
                    urlS.add(avatarUrl);
                }
                iv.addUrl(urlS);
            } else {
                for (int i = 0; i < roomMember.size(); i++) {
                    String avatarUrl = AvatarHelper.getAvatarUrl(roomMember.get(i).getUserId(), true);
                    urlS.add(avatarUrl);
                }
                iv.addUrl(urlS);
            }
        }
        lv = (ListView) mMenuView.findViewById(R.id.pop_list);
        mSetAtMemberAdapter = new SetAtMemberAdapter(context);
        mSetAtMemberAdapter.setData(roomMember);
        lv.setAdapter(mSetAtMemberAdapter);
        TextView tv1 = (TextView) mMenuView.findViewById(R.id.tv_center_filter);
        tv1.setText(InternationalizationHelper.getString("SELECT_CONSTANTS"));
        mEditText = (EditText) mMenuView.findViewById(R.id.search_et);
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
                String mContent = mEditText.getText().toString();
                List<RoomMember> roomMembers = new ArrayList<>();
                if (TextUtils.isEmpty(mContent)) {
                    mSetAtMemberAdapter.setData(roomMember);
                }
                for (int i = 0; i < roomMember.size(); i++) {
                    if (getName(roomMember.get(i)).contains(mContent)) {
                        // 符合搜索条件的好友
                        roomMembers.add((roomMember.get(i)));
                    }
                }
                mSetAtMemberAdapter.setData(roomMembers);
            }
        });
        mEveryOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSendMember.sendEveryOne("@" + "全体成员" + " ");
                dismiss();
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RoomMember member = (RoomMember) mSetAtMemberAdapter.getItem(i);
                mSendMember.sendAtContent(member);// 不能对Member的Name赋值，否则传过去的会变为自己的备注，只有自己一个人能看懂...
                dismiss();
            }
        });

    }

    private String getName(RoomMember member) {
        if (mRole == 1) {
            if (!TextUtils.equals(member.getUserName(), member.getCardName())) {// 当userName与cardName不一致时，我们认为群主有设置群内备注
                return member.getCardName();
            } else {
                if (mRemarksMap.containsKey(member.getUserId())) {
                    return mRemarksMap.get(member.getUserId());
                } else {
                    return member.getUserName();
                }
            }
        } else {
            if (mRemarksMap.containsKey(member.getUserId())) {
                return mRemarksMap.get(member.getUserId());
            } else {
                return member.getUserName();
            }
        }
    }

    public interface SendMember {
        void sendAtContent(RoomMember member);

        void sendEveryOne(String everyOne);
    }

    private class SetAtMemberAdapter extends BaseAdapter {

        private List<RoomMember> mMembers;
        private Context mContext;

        public SetAtMemberAdapter(Context context) {
            mMembers = new ArrayList<>();
            mContext = context;
        }

        public void setData(List<RoomMember> members) {
            mMembers = members;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mMembers.size();
        }

        @Override
        public Object getItem(int i) {
            return mMembers.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.a_item_set_manager, viewGroup, false);
            }
            ImageView avatar_img = ViewHolder.get(view, R.id.set_manager_iv);
            TextView roleS = ViewHolder.get(view, R.id.roles);
            TextView nick_name_tv = ViewHolder.get(view, R.id.set_manager_tv);
            ViewHolder.get(view, R.id.catagory_title).setVisibility(View.GONE);
            // 设置头像
            AvatarHelper.getInstance().displayAvatar(mMembers.get(i).getUserId(), avatar_img, true);
            roleS.setVisibility(View.GONE);
            // 设置昵称
            nick_name_tv.setText(getName(mMembers.get(i)));
            return view;
        }
    }
}
