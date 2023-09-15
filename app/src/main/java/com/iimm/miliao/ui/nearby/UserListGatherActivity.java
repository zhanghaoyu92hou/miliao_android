package com.iimm.miliao.ui.nearby;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.MyFragmentManager;

public class UserListGatherActivity extends BaseActivity {
    private MyFragmentManager mMyFragmentManager;
    private UserListGatherFragment userListGatherFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list_gather);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(InternationalizationHelper.getString("JX_Seach"));
        mMyFragmentManager = new MyFragmentManager(this, R.id.fl_fragments);
        userListGatherFragment = new UserListGatherFragment();
        mMyFragmentManager.add(userListGatherFragment);
        mMyFragmentManager.show(0);

    }
}
