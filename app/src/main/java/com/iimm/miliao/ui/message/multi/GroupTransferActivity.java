package com.iimm.miliao.ui.message.multi;

import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.RoomMember;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.RoomMemberDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.sortlist.BaseComparator;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.sortlist.SideBar;
import com.iimm.miliao.sortlist.SortHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ViewHolder;
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
 * 转让群组
 */
public class GroupTransferActivity extends BaseActivity {
    private EditText mEditText;
    private boolean isSearch;

    private ListView mListView;
    private GroupTransferAdapter mAdapter;
    private List<BaseSortModel<RoomMember>> mSortRoomMember;
    private List<BaseSortModel<RoomMember>> mSearchSortRoomMember;
    private BaseComparator<RoomMember> mBaseComparator;

    private SideBar mSideBar;
    private TextView mTextDialog;

    private TextView mTvTitleRight;

    private String mRoomId;
    private String mRoomJid;
    private String mSelectedUserId;
    private String mSelectedUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        mRoomId = getIntent().getStringExtra("roomId");
        mRoomJid = getIntent().getStringExtra("roomJid");

        initActionBar();
        initData();
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
        tvTitle.setText(getString(R.string.group_member));

        mTvTitleRight = (TextView) findViewById(R.id.tv_title_right);
        mTvTitleRight.setAlpha(0.5f);
        mTvTitleRight.setText(getString(R.string.sure));
        mTvTitleRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.commonDialog(GroupTransferActivity.this, getResources().getString(R.string.transfer_owner),
                        getResources().getString(R.string.transfer_owner_hint, mSelectedUserName), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                transferGroup();
                            }
                        });


//
//                SelectionFrame selectionFrame = new SelectionFrame(mContext);
//                selectionFrame.setSomething(null, getString(R.string.tip_set_group_owner_place_holder, mSelectedUserName), new SelectionFrame.OnSelectionFrameClickListener() {
//                    @Override
//                    public void cancelClick() {
//
//                    }
//                    @Override
//                    public void confirmClick() {
//                        transferGroup();
//                    }
//                });
//                selectionFrame.show();
            }
        });
        mTvTitleRight.setClickable(false);

        mSideBar = (SideBar) findViewById(R.id.sidebar);
    }

    private void initData() {
        mSortRoomMember = new ArrayList<>();
        mSearchSortRoomMember = new ArrayList<>();
        mBaseComparator = new BaseComparator<>();

        DialogHelper.showDefaulteMessageProgressDialog(this);
        AsyncUtils.doAsync(this, e -> {
            Reporter.post("加载数据失败，", e);
            AsyncUtils.runOnUiThread(this, ctx -> {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(ctx, R.string.data_exception);
            });
        }, c -> {
            // TODO: sql层直接过滤掉群主本人，
            List<RoomMember> data = RoomMemberDao.getInstance().getRoomMemberForTransfer(mRoomId, coreManager.getSelf().getUserId());
            Map<String, Integer> existMap = new HashMap<>();
            List<BaseSortModel<RoomMember>> sortedList = SortHelper.toSortedModelList(data, existMap, member -> {
                String name = member.getCardName();
                return name;
            });
            c.uiThread(r -> {
                DialogHelper.dismissProgressDialog();
                mSideBar.setExistMap(existMap);
                mSortRoomMember = sortedList;
                mAdapter.setData(sortedList);
            });
        });

    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.list_view);
        mAdapter = new GroupTransferAdapter(mSortRoomMember);
        mListView.setAdapter(mAdapter);

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

        mEditText = (EditText) findViewById(R.id.search_edit);
//        mEditText.setHint(InternationalizationHelper.getString("JX_Seach"));
        mEditText.setHint(getString(R.string.search_for_contacts));
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
                mSearchSortRoomMember.clear();
                String str = mEditText.getText().toString();
                if (TextUtils.isEmpty(str)) {
                    isSearch = false;
                    mAdapter.setData(mSortRoomMember);
                    return;
                }
                for (int i = 0; i < mSortRoomMember.size(); i++) {
                    if (mSortRoomMember.get(i).getBean().getCardName().contains(str)) { // 符合搜索条件的好友
                        mSearchSortRoomMember.add((mSortRoomMember.get(i)));
                    }
                }
                mAdapter.setData(mSearchSortRoomMember);
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                BaseSortModel<RoomMember> baseSortModel;
                if (isSearch) {
                    baseSortModel = mSearchSortRoomMember.get(position);
                } else {
                    baseSortModel = mSortRoomMember.get(position);
                }
                RoomMember roomMember = baseSortModel.bean;
                mSelectedUserId = roomMember.getUserId();
                mSelectedUserName = roomMember.getCardName();
                mAdapter.notifyDataSetChanged();

                mTvTitleRight.setAlpha(1.0f);
                mTvTitleRight.setClickable(true);
            }
        });
    }

    /**
     * 转让群主
     */
    private void transferGroup() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        params.put("toUserId", mSelectedUserId);
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_TRANSFER)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            FriendDao.getInstance().updateRoomCreateUserId(coreManager.getSelf().getUserId(),
                                    mRoomJid, mSelectedUserId);
                            RoomMemberDao.getInstance().updateRoomMemberRole(mRoomId, coreManager.getSelf().getUserId(), Constants.ROLE_MEMBER);
                            EventBus.getDefault().post(new EventGroupStatus(10002, 0));
                            finish();
                        } else {
                            ToastUtil.showErrorData(mContext);
                        }
                    }
                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    class GroupTransferAdapter extends BaseAdapter implements SectionIndexer {
        List<BaseSortModel<RoomMember>> mSortRoomMember;

        GroupTransferAdapter(List<BaseSortModel<RoomMember>> sortRoomMember) {
            this.mSortRoomMember = new ArrayList<>();
            this.mSortRoomMember = sortRoomMember;
        }

        public void setData(List<BaseSortModel<RoomMember>> sortRoomMember) {
            this.mSortRoomMember = sortRoomMember;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mSortRoomMember.size();
        }

        @Override
        public Object getItem(int position) {
            return mSortRoomMember.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_transfer, parent, false);
            }
            TextView catagoryTitleTv = ViewHolder.get(convertView, R.id.catagory_title);
            ImageView avatarImg = ViewHolder.get(convertView, R.id.avatar_img);
            TextView roleS = ViewHolder.get(convertView, R.id.roles);
            TextView userNameTv = ViewHolder.get(convertView, R.id.user_name_tv);
            ImageView mSelectedIv = ViewHolder.get(convertView, R.id.selected_iv);

            // 根据position获取分类的首字母的Char ascii值
            int section = getSectionForPosition(position);
            // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (position == getPositionForSection(section)) {
                catagoryTitleTv.setVisibility(View.VISIBLE);
                catagoryTitleTv.setText(mSortRoomMember.get(position).getFirstLetter());
            } else {
                catagoryTitleTv.setVisibility(View.GONE);
            }
            RoomMember member = mSortRoomMember.get(position).getBean();
            if (member != null) {
                AvatarHelper.getInstance().displayAvatar(member.getUserId(), avatarImg, true);
                if (member.getRole() == 1) {
                    roleS.setBackgroundResource(R.drawable.bg_role1);
                    roleS.setText(InternationalizationHelper.getString("JXGroup_Owner"));
                } else if (member.getRole() == 2) {
                    roleS.setBackgroundResource(R.drawable.bg_role2);
                    roleS.setText(InternationalizationHelper.getString("JXGroup_Admin"));
                } else {
                    roleS.setBackgroundResource(R.drawable.bg_role3);
                    roleS.setText(InternationalizationHelper.getString("JXGroup_RoleNormal"));
                }
                userNameTv.setText(member.getCardName());
                if (!TextUtils.isEmpty(mSelectedUserId) && member.getUserId().equals(mSelectedUserId)) {
                    mSelectedIv.setVisibility(View.VISIBLE);
                } else {
                    mSelectedIv.setVisibility(View.GONE);
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
                String sortStr = mSortRoomMember.get(i).getFirstLetter();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getSectionForPosition(int position) {
            return mSortRoomMember.get(position).getFirstLetter().charAt(0);
        }
    }
}
