package com.iimm.miliao.ui.nearby;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.util.ToastUtil;

public class PublicNumberSearchActivity extends BaseActivity {
    private EditText mKeyWordEdit;
    private Button mSearchBtn;

    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, PublicNumberSearchActivity.class);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_number_search);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(R.string.search_public_number);
        initView();
    }

    private void initView() {
        mKeyWordEdit = (EditText) findViewById(R.id.keyword_edit);
        // 获取焦点，键盘弹出
        mKeyWordEdit.requestFocus();

        mSearchBtn = (Button) findViewById(R.id.search_btn);
        // mSearchBtn.setBackgroundColor(SkinUtils.getSkin(this).getAccentColor());

        mSearchBtn.setOnClickListener(v -> {
            if (TextUtils.isEmpty(mKeyWordEdit.getText().toString())){
                ToastUtil.showToast(PublicNumberSearchActivity.this,"请输入公众号");
            }else {
                PublicNumberListActivity.start(mContext, mKeyWordEdit.getText().toString());
            }

        });
    }
}
