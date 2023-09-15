package com.iimm.miliao.ui.contacts.label;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.makeramen.roundedimageview.RoundedImageView;
import com.iimm.miliao.R;
import com.iimm.miliao.Reporter;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.Label;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.db.dao.LabelDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.sortlist.BaseComparator;
import com.iimm.miliao.sortlist.BaseSortModel;
import com.iimm.miliao.sortlist.SideBar;
import com.iimm.miliao.sortlist.SortHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.AsyncUtils;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.DisplayUtil;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ViewHolder;
import com.iimm.miliao.view.HorizontalListView;
import com.iimm.miliao.view.SelectionFrame;
import com.iimm.miliao.view.VerifyDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class SelectLabelFriendActivity extends BaseActivity {
    private EditText mEditText;
    private boolean isSearch;

    private ListView mListView;
    private ListViewAdapter mAdapter;
    private List<Friend> mFriendList;
    private List<BaseSortModel<Friend>> mSortFriends;
    private List<BaseSortModel<Friend>> mSearchSortFriends;
    private BaseComparator<Friend> mBaseComparator;

    private SideBar mSideBar;
    private TextView mTextDialog;

    private HorizontalListView mHorizontalListView;
    private HorListViewAdapter mHorAdapter;
    private List<String> mSelectPositions;
    private Button mOkBtn;

    private String mLoginUserId;
    private List<String> mExistIds;
    private TextView mTitleRight;

    /**
     * Todo add 2018.6.20 intent from 发布说说-谁可以看
     */
    private boolean isFromSeeCircleActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);
        if (getIntent() != null) {
            isFromSeeCircleActivity = getIntent().getBooleanExtra("IS_FROM_SEE_CIRCLE_ACTIVITY", false);
            String ids = getIntent().getStringExtra("exist_ids");
            mExistIds = JSON.parseArray(ids, String.class);
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
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(InternationalizationHelper.getString("SELECT_CONTANTS"));
        mTitleRight = findViewById(R.id.tv_title_right);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTitleRight.getLayoutParams();
        params.width = DisplayUtil.dip2px(this, 43);
        params.height = DisplayUtil.dip2px(this, 28);
        mTitleRight.setLayoutParams(params);

        mTitleRight.setVisibility(View.VISIBLE);
        mTitleRight.setText("确定");
        mTitleRight.setTextSize(14);
        mTitleRight.setBackgroundResource(R.drawable.bg_tight_tv);
        mTitleRight.setTextColor(getResources().getColor(android.R.color.white));


    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setAdapter(mAdapter);
        mHorizontalListView = (HorizontalListView) findViewById(R.id.horizontal_list_view);
        mHorizontalListView.setAdapter(mHorAdapter);
        mOkBtn = (Button) findViewById(R.id.ok_btn);
        // mOkBtn.setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());

        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mSideBar.setVisibility(View.GONE);
        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar.setTextView(mTextDialog);
        mSideBar.setOnTouchingLetterChangedListener(s -> {
            // 该字母首次出现的位置
            int position = mAdapter.getPositionForSection(s.charAt(0));
            if (position != -1) {
                mListView.setSelection(position);
            }
        });
        findViewById(R.id.added_layout).setVisibility(View.GONE);  //不要底部，否则和发起群聊 界面底部有冲突


        mEditText = (EditText) findViewById(R.id.search_et);
//        mEditText.setHint(InternationalizationHelper.getString("JX_Seach"));
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
                    String name = !TextUtils.isEmpty(mSortFriends.get(i).getBean().getRemarkName()) ?
                            mSortFriends.get(i).getBean().getRemarkName() : mSortFriends.get(i).getBean().getNickName();
                    if (name.contains(str)) {
                        // 符合搜索条件的好友
                        mSearchSortFriends.add((mSortFriends.get(i)));
                    }
                }
                mAdapter.setData(mSearchSortFriends);
            }
        });

        mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() + ""));

        mListView.setOnItemClickListener((arg0, arg1, position, arg3) -> {
            Friend friend;
            if (isSearch) {
                friend = mSearchSortFriends.get(position).bean;
            } else {
                friend = mSortFriends.get(position).bean;
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
        });

        mHorizontalListView.setOnItemClickListener((arg0, arg1, position, arg3) -> {
            for (int i = 0; i < mSortFriends.size(); i++) {
                if (mSortFriends.get(i).getBean().getUserId().equals(mSelectPositions.get(position))) {
                    mSortFriends.get(i).getBean().setStatus(101);
                    mAdapter.setData(mSortFriends);
                }
            }
            mSelectPositions.remove(position);
            mHorAdapter.notifyDataSetInvalidated();
            mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() + ""));
        });

        mTitleRight.setOnClickListener(v -> {
            if (mSelectPositions.size() <= 0) {
                Toast.makeText(SelectLabelFriendActivity.this, R.string.tip_select_at_lease_one_contacts, Toast.LENGTH_SHORT).show();
                return;
            }

            final List<String> inviteIdList = new ArrayList<>();
            final List<String> inviteNameList = new ArrayList<>();
            // 邀请好友
            for (int i = 0; i < mSelectPositions.size(); i++) {
                inviteIdList.add(mSelectPositions.get(i));
                for (Friend friend : mFriendList) {
                    if (friend.getUserId().equals(mSelectPositions.get(i))) {
                        inviteNameList.add(!TextUtils.isEmpty(friend.getRemarkName()) ? friend.getRemarkName() : friend.getNickName());
                    }
                }
            }
            // ["10004541","10007042"]
            final String inviteId = JSON.toJSONString(inviteIdList);
            // ["阿飞","小明"]
            final String inviteName = JSON.toJSONString(inviteNameList);
            Log.e("zq", inviteId);
            Log.e("zq", inviteName);

            if (isFromSeeCircleActivity) {
                SelectionFrame selectionFrame = new SelectionFrame(SelectLabelFriendActivity.this);
                selectionFrame.setSomething(null, getString(R.string.tip_save_tag), getString(R.string.ignore), getString(R.string.save_tag), new SelectionFrame.OnSelectionFrameClickListener() {
                    @Override
                    public void cancelClick() {
                        Intent intent = new Intent();
                        intent.putExtra("inviteId", inviteId);
                        intent.putExtra("inviteName", inviteName);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    public void confirmClick() {
                        VerifyDialog verifyDialog = new VerifyDialog(SelectLabelFriendActivity.this);
                        verifyDialog.setVerifyClickListener(getString(R.string.tag_name), new VerifyDialog.VerifyClickListener() {
                            @Override
                            public void cancel() {

                            }

                            @Override
                            public void send(String str) {
                                createLabel(str, inviteIdList);
                            }
                        });
                        verifyDialog.show();
                    }
                });
                selectionFrame.show();
            } else {
                Intent intent = new Intent();
                intent.putExtra("inviteId", inviteId);
                intent.putExtra("inviteName", inviteName);
                setResult(RESULT_OK, intent);
                finish();
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
            final List<Friend> friends = FriendDao.getInstance().getAllFriends(mLoginUserId);
            for (int i = 0; i < friends.size(); i++) {
                boolean isExist = isExist(friends.get(i));
                if (isFromSeeCircleActivity) { // isFromSeeCircleActivity true:选中 false: 移除
                    if (isExist) {
                        friends.get(i).setStatus(100);
                        mSelectPositions.add(friends.get(i).getUserId());
                    }
                } else {
                    if (isExist) {
                        friends.remove(i);
                        i--;
                    }
                }
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

    private boolean isExist(Friend friend) {
        for (int i = 0; i < mExistIds.size(); i++) {
            if (mExistIds.get(i) == null) {
                continue;
            }
            if (friend.getUserId().equals(mExistIds.get(i))) {
                return true;
            }
        }
        return false;
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

    /**
     * Todo  isFromSeeCircleActivity 专属 HTTP 请求
     */
    private void createLabel(String groupName, final List<String> inviteIdList) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("groupName", groupName);
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().FRIENDGROUP_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<Label>(Label.class) {
                    @Override
                    public void onResponse(ObjectResult<Label> result) {
                        if (result.getResultCode() == 1) {
                            LabelDao.getInstance().createLabel(result.getData());
                            updateLabelUserIdList(result.getData(), inviteIdList);
                        } else {
                            DialogHelper.dismissProgressDialog();
                            ToastUtil.showErrorData(SelectLabelFriendActivity.this);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(SelectLabelFriendActivity.this);
                    }
                });
    }

    private void updateLabelUserIdList(final Label label, List<String> inviteIdList) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("groupId", label.getGroupId());
        String userIdListStr = "";
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < inviteIdList.size(); i++) {
            if (i == inviteIdList.size() - 1) {
                userIdListStr += inviteIdList.get(i);
            } else {
                userIdListStr += inviteIdList.get(i) + ",";
            }
            strings.add(inviteIdList.get(i));
        }
        params.put("userIdListStr", userIdListStr);

        final String userIdList = JSON.toJSONString(strings);

        HttpUtils.get().url(coreManager.getConfig().FRIENDGROUP_UPDATEGROUPUSERLIST)
                .params(params)
                .build()
                .execute(new BaseCallback<Label>(Label.class) {
                    @Override
                    public void onResponse(ObjectResult<Label> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            LabelDao.getInstance().updateLabelUserIdList(mLoginUserId, label.getGroupId(), userIdList);
                            Intent intent = new Intent();
                            intent.putExtra("NEW_LABEL_ID", label.getGroupId());
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            ToastUtil.showErrorData(SelectLabelFriendActivity.this);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(SelectLabelFriendActivity.this);
                    }
                });
    }

    class ListViewAdapter extends BaseAdapter implements SectionIndexer {
        List<BaseSortModel<Friend>> mSortFriends;

        ListViewAdapter() {
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_select_contacts_clone, parent, false);
            }
            TextView catagoryTitleTv = ViewHolder.get(convertView, R.id.catagory_title);
            ImageView avatarImg = ViewHolder.get(convertView, R.id.avatar_img);
            TextView userNameTv = ViewHolder.get(convertView, R.id.user_name_tv);
            CheckBox checkBox = ViewHolder.get(convertView, R.id.check_box);

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
                String name = !TextUtils.isEmpty(friend.getRemarkName()) ? friend.getRemarkName() : friend.getNickName();
                userNameTv.setText(name);
                checkBox.setChecked(false);

                if (friend.getStatus() == 100) {
                    checkBox.setChecked(true);
                } else {
                    checkBox.setChecked(false);
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

    class HorListViewAdapter extends BaseAdapter {
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
