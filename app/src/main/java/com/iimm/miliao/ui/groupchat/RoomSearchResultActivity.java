package com.iimm.miliao.ui.groupchat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.ui.base.BaseActivity;

public class RoomSearchResultActivity extends BaseActivity {

    public static void start(Context ctx, String keyWord) {
        Intent intent = new Intent(ctx, RoomSearchResultActivity.class);
        intent.putExtra("roomName", keyWord);
        ctx.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_search_result);
        getSupportActionBar().hide();
        TextView mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(InternationalizationHelper.getString("GROUP"));
    }
}
