package com.iimm.miliao.ui.company;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.bean.Friend;
import com.iimm.miliao.bean.company.StructBeanNetInfo;
import com.iimm.miliao.db.dao.FriendDao;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.DisplayUtil;
import com.iimm.miliao.util.EventBusCommpanyContorl;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ViewHolder;
import com.iimm.miliao.view.CircleImageView;
import com.iimm.miliao.view.HorizontalListView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;


public class AddEmployee extends BaseActivity {
    private final int LAST_ICON = -1;
    List<StructBeanNetInfo.DepartmentsBean.EmployeesBean> employeesBeans;
    private ListView mListView;
    private HorizontalListView mHorizontalListView;
    private Button mOkBtn;
    private List<Friend> mFriendList;
    private ListViewAdapter mAdapter;
    private List<Integer> mSelectPositions;
    private HorListViewAdapter mHorAdapter;
    // 该部门下的所有员工的userId
    private int departmentPosition;
    private StructBeanNetInfo structBeanNetInfo;
    private String mLoginUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_employe);
        if (getIntent() != null) {
            departmentPosition = getIntent().getIntExtra("departmentPosition", 0);
            structBeanNetInfo = (StructBeanNetInfo) getIntent().getExtras().getSerializable("data");
        }
        mLoginUserId = coreManager.getSelf().getUserId();
        employeesBeans = new ArrayList<>();
        List<StructBeanNetInfo.DepartmentsBean> departments = structBeanNetInfo.getDepartments();
        for (int i = 0; i < departments.size(); i++) {
            employeesBeans.addAll(departments.get(i).getEmployees());

        }
        mFriendList = new ArrayList<Friend>();
        mAdapter = new ListViewAdapter();
        mSelectPositions = new ArrayList<Integer>();
        // 增加一个虚线框的位置
        mSelectPositions.add(LAST_ICON);
        mHorAdapter = new HorListViewAdapter();
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<Friend> userInfos = FriendDao.getInstance().getAllFriends(mLoginUserId);
        if (userInfos != null) {
            mFriendList.clear();
            for (int i = 0; i < userInfos.size(); i++) {
                boolean isIn = isExist(userInfos.get(i));
                if (isIn) {
                    userInfos.remove(i);
                    i--;
                } else {
                    mFriendList.add(userInfos.get(i));
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 该部门下是否存在该用户，如果存在则不加载
     */
    private boolean isExist(Friend friend) {

        for (int i = 0; i < employeesBeans.size(); i++) {
            if (employeesBeans.get(i) == null) {
                continue;
            }
            if (friend.getUserId().equals(employeesBeans.get(i).getUserId() + "")) {
                return true;
            }
        }
        return false;
    }

    private void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.add_employee);

        mListView = (ListView) findViewById(R.id.list_view);
        mHorizontalListView = (HorizontalListView) findViewById(R.id.horizontal_list_view);
        mOkBtn = (Button) findViewById(R.id.ok_btn);
        // mOkBtn.setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());

        mListView.setAdapter(mAdapter);
        mHorizontalListView.setAdapter(mHorAdapter);
        mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() - 1));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (hasSelected(position)) {
                    removeSelect(position);
                } else {
                    addSelect(position);
                }
            }
        });

        mHorizontalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (position == mSelectPositions.size() - 1) {
                    return;
                }
                mSelectPositions.remove(position);
                mAdapter.notifyDataSetInvalidated();
                mHorAdapter.notifyDataSetInvalidated();
                mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() - 1));
            }
        });

        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inviteFriend(structBeanNetInfo.getId(), structBeanNetInfo.getDepartments().get(departmentPosition).getId());
            }
        });
    }

    private void addSelect(int position) {
        if (!hasSelected(position)) {
            mSelectPositions.add(0, position);
            mAdapter.notifyDataSetInvalidated();
            mHorAdapter.notifyDataSetInvalidated();
            mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() - 1));
        }
    }

    private boolean hasSelected(int position) {
        for (int i = 0; i < mSelectPositions.size(); i++) {
            if (mSelectPositions.get(i) == position) {
                return true;
            } else if (i == mSelectPositions.size() - 1) {
                return false;
            }
        }
        return false;
    }

    private void removeSelect(int position) {
        mSelectPositions.remove(Integer.valueOf(position));
        mAdapter.notifyDataSetInvalidated();
        mHorAdapter.notifyDataSetInvalidated();
        mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() - 1));
    }

    /**
     * 添加成员
     */
    private void inviteFriend(String companyId, String departmentId) {
        if (mSelectPositions.size() <= 1) {
            finish();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("companyId", companyId);
        params.put("departmentId", departmentId);
        String str = "";
        // 邀请好友
        for (int i = 0; i < mSelectPositions.size(); i++) {
            if (mSelectPositions.get(i) == -1) {
                continue;
            }
            String userId = mFriendList.get(mSelectPositions.get(i)).getUserId();
            str += userId + ",";
        }
        String userId = str.substring(0, str.length() - 1);
        params.put("userId", userId);

        HttpUtils.get().url(coreManager.getConfig().ADD_EMPLOYEE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {
                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (result.getResultCode() == 1) {
                            ToastUtil.showToast(AddEmployee.this, R.string.add_employee_succ);

                            EventBus.getDefault().post(EventBusCommpanyContorl.getInstance(structBeanNetInfo,
                                    EventBusCommpanyContorl.ADD_EMPLOYEES));// 数据有更新
                            finish();
                        } else {
                            // 添加成员失败
                            ToastUtil.showToast(AddEmployee.this, R.string.add_employee_fail);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(AddEmployee.this);
                    }
                });
    }

    private class ListViewAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mFriendList.size();
        }

        @Override
        public Object getItem(int position) {
            return mFriendList.get(position);
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
            TextView mSortTv = ViewHolder.get(convertView, R.id.catagory_title);
            mSortTv.setVisibility(View.GONE);
            ImageView avatarImg = ViewHolder.get(convertView, R.id.avatar_img);
            TextView userNameTv = ViewHolder.get(convertView, R.id.user_name_tv);
            CheckBox checkBox = ViewHolder.get(convertView, R.id.check_box);

            AvatarHelper.getInstance().displayAvatar(mFriendList.get(position).getUserId(), avatarImg, true);
            userNameTv.setText(mFriendList.get(position).getNickName());
            checkBox.setChecked(false);
            if (mSelectPositions.contains(Integer.valueOf(position))) {
                checkBox.setChecked(true);
            }
            return convertView;
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
                convertView = new CircleImageView(mContext);
                int size = DisplayUtil.dip2px(mContext, 37);
                AbsListView.LayoutParams param = new AbsListView.LayoutParams(size, size);
                convertView.setLayoutParams(param);
            }
            ImageView imageView = (ImageView) convertView;
            int selectPosition = mSelectPositions.get(position);
            if (selectPosition == -1) {
                //                imageView.setImageResource(R.drawable.dot_avatar);
            } else {
                if (selectPosition >= 0 && selectPosition < mFriendList.size()) {
                    AvatarHelper.getInstance().displayAvatar(mFriendList.get(selectPosition).getUserId(), imageView, true);
                }
            }
            return convertView;
        }
    }
}
