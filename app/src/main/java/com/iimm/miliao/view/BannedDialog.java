package com.iimm.miliao.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.iimm.miliao.R;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.util.ScreenUtil;

/**
 * 禁言
 */
public class BannedDialog extends Dialog implements View.OnClickListener {
    private TextView mNoGag, mOneGag, mTwoGag, mThreeGag, mFourGag, mFiveGag, mSixGag;
    private OnBannedDialogClickListener mOnBannedDialogClickListener;
    private View mMCancel;

    public BannedDialog(Context context, OnBannedDialogClickListener onBannedDialogClickListener) {
        super(context, R.style.BottomDialog);
        this.mOnBannedDialogClickListener = onBannedDialogClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gag_dialog);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        mNoGag = (TextView) findViewById(R.id.no_gag);
        mOneGag = (TextView) findViewById(R.id.one_gag);
        mTwoGag = (TextView) findViewById(R.id.two_gag);
        mThreeGag = (TextView) findViewById(R.id.three_gag);
        mFourGag = (TextView) findViewById(R.id.four_gag);
        mFiveGag = (TextView) findViewById(R.id.five_gag);
        mSixGag = (TextView) findViewById(R.id.six_gag);
        mMCancel = findViewById(R.id.tv_cancel);
        mNoGag.setText(InternationalizationHelper.getString("JXAlert_NotGag"));
        mOneGag.setText(R.string.ban_10m);
        mTwoGag.setText(InternationalizationHelper.getString("GayFOR_ONEHOUR"));
        mThreeGag.setText(InternationalizationHelper.getString("JXAlert_GagOne"));
        mFourGag.setText(InternationalizationHelper.getString("JXAlert_GagThere"));
        mFiveGag.setText(InternationalizationHelper.getString("JXAlert_GagOneWeek"));
        mSixGag.setText(R.string.ban_half_m);
        mNoGag.setOnClickListener(this);
        mOneGag.setOnClickListener(this);
        mTwoGag.setOnClickListener(this);
        mThreeGag.setOnClickListener(this);
        mFourGag.setOnClickListener(this);
        mFiveGag.setOnClickListener(this);
        mSixGag.setOnClickListener(this);
        mMCancel.setOnClickListener(this);

        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        // x/y坐标
        // lp.x = 100;
        // lp.y = 100;
        lp.width = ScreenUtil.getScreenWidth(getContext());
        o.setAttributes(lp);
        this.getWindow().setGravity(Gravity.BOTTOM);
        this.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        switch (v.getId()) {
            case R.id.no_gag:
                mOnBannedDialogClickListener.tv1Click();
                break;
            case R.id.one_gag:
                mOnBannedDialogClickListener.tv2Click();
                break;
            case R.id.two_gag:
                mOnBannedDialogClickListener.tv3Click();
                break;
            case R.id.three_gag:
                mOnBannedDialogClickListener.tv4Click();
                break;
            case R.id.four_gag:
                mOnBannedDialogClickListener.tv5Click();
                break;
            case R.id.five_gag:
                mOnBannedDialogClickListener.tv6Click();
                break;
            case R.id.six_gag:
                mOnBannedDialogClickListener.tv7Click();
                break;
            case R.id.cancel:
                this.dismiss();
                break;
        }
    }

    public interface OnBannedDialogClickListener {
        void tv1Click();

        void tv2Click();

        void tv3Click();

        void tv4Click();

        void tv5Click();

        void tv6Click();

        void tv7Click();
    }
}
