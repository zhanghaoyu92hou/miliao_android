package com.iimm.miliao.ui.message.assistant;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.iimm.miliao.R;
import com.iimm.miliao.bean.EventNotifyByTag;
import com.iimm.miliao.bean.assistant.GroupAssistant;
import com.iimm.miliao.bean.assistant.GroupAssistantDetail;
import com.iimm.miliao.helper.AvatarHelper;
import com.iimm.miliao.helper.DialogHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.base.CoreManager;
import com.iimm.miliao.util.ToastUtil;
import com.iimm.miliao.util.ViewHolder;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

public class SelectGroupAssistantActivity extends BaseActivity {
    private PullToRefreshListView mPullToRefreshListView;
    private GroupAssistantAdapter mGroupAssistantAdapter;
    private List<GroupAssistant> mGroupAssistantData = new ArrayList<>();
    private Map<String, String> mExistedAssistantMap = new HashMap<>();

    private String roomId, roomJid;

    public static void start(Context context, String roomId, String roomJid) {
        Intent intent = new Intent(context, SelectGroupAssistantActivity.class);
        intent.putExtra("roomId", roomId);
        intent.putExtra("roomJid", roomJid);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group_assistant);
        roomId = getIntent().getStringExtra("roomId");
        roomJid = getIntent().getStringExtra("roomJid");

        initActionBar();
        getExistedAssistants();
        initView();
        initData();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.group_assistant));
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final EventNotifyByTag message) {
        if (TextUtils.equals(message.tag, "GroupAssistant")) {
            getExistedAssistants();
        }
    }

    /**
     * 获取当前群组已经添加的群助手
     */
    private void getExistedAssistants() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(mContext).accessToken);
        params.put("roomId", roomId);

        HttpUtils.get().url(CoreManager.requireConfig(mContext).ROOM_QUERY_GROUP_HELPER)
                .params(params)
                .build()
                .execute(new ListCallback<GroupAssistantDetail>(GroupAssistantDetail.class) {
                    @Override
                    public void onResponse(ArrayResult<GroupAssistantDetail> result) {
                        if (result != null && result.getResultCode() == 1) {
                            mExistedAssistantMap.clear();
                            for (int i = 0; i < result.getData().size(); i++) {
                                mExistedAssistantMap.put(result.getData().get(i).getHelperId(), result.getData().get(i).getHelperId());
                            }
                            if (mGroupAssistantAdapter != null) {
                                mGroupAssistantAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private void initView() {
        mPullToRefreshListView = findViewById(R.id.pull_refresh_list);
        mGroupAssistantAdapter = new GroupAssistantAdapter();
        mPullToRefreshListView.setAdapter(mGroupAssistantAdapter);
        mPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GroupAssistant groupAssistant = mGroupAssistantData.get((int) id);
                if (groupAssistant != null) {
                    GroupAssistantDetailActivity.start(mContext, roomId, roomJid, JSON.toJSONString(groupAssistant));
                }
            }
        });
    }

    private void initData() {
        DialogHelper.showDefaulteMessageProgressDialog(mContext);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", CoreManager.requireSelfStatus(mContext).accessToken);

        HttpUtils.get().url(CoreManager.requireConfig(mContext).OPEN_GET_HELPER_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<GroupAssistant>(GroupAssistant.class) {
                    @Override
                    public void onResponse(ArrayResult<GroupAssistant> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result != null && result.getData() != null) {
                            mGroupAssistantData.addAll(result.getData());
                            mGroupAssistantAdapter.notifyDataSetChanged();
                        } else {
                            if (result != null && !TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(mContext, result.getResultMsg());
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    class GroupAssistantAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mGroupAssistantData.size();
        }

        @Override
        public Object getItem(int position) {
            return mGroupAssistantData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_group_assistant, parent, false);
            }
            ImageView avatar = ViewHolder.get(convertView, R.id.avatar);
            TextView name = ViewHolder.get(convertView, R.id.name);
            TextView desc = ViewHolder.get(convertView, R.id.desc);
            Button add = ViewHolder.get(convertView, R.id.add);

            GroupAssistant groupAssistant = mGroupAssistantData.get(position);
            if (groupAssistant != null) {
                if (mExistedAssistantMap.containsKey(groupAssistant.getId())) {
                    add.setVisibility(View.GONE);
                } else {
                    add.setVisibility(View.VISIBLE);
                }

                AvatarHelper.getInstance().displayUrl(groupAssistant.getIconUrl(), avatar);
                name.setText(groupAssistant.getName());
                desc.setText(groupAssistant.getDesc());
                add.setOnClickListener(v -> addGroupAssistant(groupAssistant));
            }
            return convertView;
        }

        private void addGroupAssistant(GroupAssistant groupAssistant) {
            DialogHelper.showDefaulteMessageProgressDialog(mContext);

            Map<String, String> params = new HashMap<>();
            params.put("access_token", CoreManager.requireSelfStatus(mContext).accessToken);
            params.put("helperId", groupAssistant.getId());
            params.put("roomId", roomId);
            params.put("roomJid", roomJid);

            HttpUtils.get().url(CoreManager.requireConfig(mContext).ROOM_ADD_GROUP_HELPER)
                    .params(params)
                    .build()
                    .execute(new BaseCallback<Void>(Void.class) {

                        @Override
                        public void onResponse(ObjectResult<Void> result) {
                            DialogHelper.dismissProgressDialog();
                            if (result != null && result.getResultCode() == 1) {
                                mExistedAssistantMap.put(groupAssistant.getId(), groupAssistant.getId());
                                mGroupAssistantAdapter.notifyDataSetChanged();
                                ToastUtil.showToast(mContext, getString(R.string.add_to) + getString(R.string.success));
                                EventBus.getDefault().post(new EventNotifyByTag("GroupAssistant"));
                            } else {
                                if (result != null && !TextUtils.isEmpty(result.getResultMsg())) {
                                    ToastUtil.showToast(mContext, result.getResultMsg());
                                }
                            }
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                            DialogHelper.dismissProgressDialog();
                            ToastUtil.showErrorNet(mContext);
                        }
                    });
        }
    }
}
