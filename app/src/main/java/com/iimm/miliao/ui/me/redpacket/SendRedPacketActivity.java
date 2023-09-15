package com.iimm.miliao.ui.me.redpacket;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gyf.immersionbar.ImmersionBar;
import com.iimm.miliao.R;
import com.iimm.miliao.db.InternationalizationHelper;
import com.iimm.miliao.ui.base.BaseActivity;
import com.iimm.miliao.ui.message.ChatActivity;
import com.iimm.miliao.ui.smarttab.SmartTabLayout;
import com.iimm.miliao.util.Constants;
import com.iimm.miliao.util.InputChangeListenerNew;
import com.iimm.miliao.util.PreferenceUtils;
import com.iimm.miliao.util.ToastUtil;

import org.jivesoftware.smack.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 发红包
 */
public class SendRedPacketActivity extends BaseActivity implements View.OnClickListener {
    LayoutInflater inflater;
    private SmartTabLayout smartTabLayout;
    private ViewPager viewPager;
    private List<View> views;
    private List<String> mTitleList;
    private EditText editTextPt;  // 普通红包的金额输入框
    private EditText editTextKl;  // 口令红包的金额输入框
    private EditText editTextPwd; // 口令输入框
    private EditText editTextGre; // 祝福语输入框
    private TextView money_tv;
    private TextView MoneyKl;
    private View mPlaceholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redpacket);

        inflater = LayoutInflater.from(this);
        initView();

        checkHasPayPassword();

        mPlaceholder = findViewById(R.id.placeholder_v);
        ImmersionBar.with(this)
                .statusBarView(mPlaceholder)
                .statusBarDarkFont(true, 0.2f)
                .init();
    }

    private void checkHasPayPassword() {
        boolean hasPayPassword = PreferenceUtils.getBoolean(this, Constants.IS_PAY_PASSWORD_SET + coreManager.getSelf().getUserId(), true);
        if (!hasPayPassword) {
            Intent intent = new Intent(this, ChangePayPasswordActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * 初始化布局
     */
    private void initView() {
        getSupportActionBar().hide();
        findViewById(R.id.tv_title_left).setOnClickListener(v -> finish());

        viewPager = (ViewPager) findViewById(R.id.viewpagert_redpacket);
        smartTabLayout = (SmartTabLayout) findViewById(R.id.smarttablayout_redpacket);
        views = new ArrayList<View>();
        mTitleList = new ArrayList<String>();
        mTitleList.add(InternationalizationHelper.getString("JX_UsualGift"));
        mTitleList.add(InternationalizationHelper.getString("JX_MesGift"));
        View v1, v2;
        v1 = inflater.inflate(R.layout.redpacket_pager_pt, null);
        v2 = inflater.inflate(R.layout.redpacket_pager_kl, null);
        views.add(v1);
        views.add(v2);

        // 获取EditText
        editTextPt = (EditText) v1.findViewById(R.id.edit_money);
        editTextGre = (EditText) v1.findViewById(R.id.edit_blessing);
        money_tv = v1.findViewById(R.id.money_tv);
        editTextKl = (EditText) v2.findViewById(R.id.edit_money);
        editTextPwd = (EditText) v2.findViewById(R.id.edit_password);
        MoneyKl = v2.findViewById(R.id.money_tv);

        TextView jineTv, tipTv, sumjineTv, yuan1, yuan2;
        jineTv = (TextView) v1.findViewById(R.id.JinETv);
        tipTv = (TextView) v2.findViewById(R.id.textviewtishi);
        sumjineTv = (TextView) v2.findViewById(R.id.sumMoneyTv);
        yuan1 = (TextView) v1.findViewById(R.id.yuanTv);
        yuan2 = (TextView) v2.findViewById(R.id.yuanTv);
//        jineTv.setText(InternationalizationHelper.getString("AMOUNT_OF_MONEY"));
        tipTv.setText(InternationalizationHelper.getString("SMALL_PARTNERS"));
        sumjineTv.setText(InternationalizationHelper.getString("TOTAL_AMOUNT"));
        yuan1.setText(InternationalizationHelper.getString("YUAN"));
        yuan2.setText(InternationalizationHelper.getString("YUAN"));

//        editTextPt.setHint(InternationalizationHelper.getString("JX_InputGiftCount"));
//        editTextGre.setHint(InternationalizationHelper.getString("JX_GiftText"));

//        editTextKl.setHint(InternationalizationHelper.getString("JX_InputGiftCount"));
//        editTextPwd.setHint(InternationalizationHelper.getString("JX_WantOpenGift"));

        TextView koulinTv;
        koulinTv = (TextView) v2.findViewById(R.id.setKouLinTv);
//        koulinTv.setText(InternationalizationHelper.getString("JX_Message"));

        Button b1 = (Button) v1.findViewById(R.id.btn_sendRed);
        b1.setOnClickListener(this);
        Button b2 = (Button) v2.findViewById(R.id.btn_sendRed);
        b2.setOnClickListener(this);

        b1.requestFocus();
        b1.setClickable(true);
        b2.requestFocus();
        b2.setClickable(true);

        InputChangeListenerNew inputChangeListenerPt = new InputChangeListenerNew(editTextPt, money_tv);
        InputChangeListenerNew inputChangeListenerKl = new InputChangeListenerNew(editTextKl, MoneyKl);

        editTextPt.addTextChangedListener(inputChangeListenerPt);
        editTextKl.addTextChangedListener(inputChangeListenerKl);

        //设置值允许输入数字和小数点
        editTextPt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editTextKl.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        viewPager.setAdapter(new PagerAdapter());
        inflater = LayoutInflater.from(this);
        smartTabLayout.setViewPager(viewPager);

        /**
         * 为了实现点击Tab栏切换的时候不出现动画
         * 为每个Tab重新设置点击事件
         */
        for (int i = 0; i < mTitleList.size(); i++) {
            View view = smartTabLayout.getTabAt(i);
            view.setTag(i + "");
            view.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_sendRed) {
            final Bundle bundle = new Bundle();
            final Intent data = new Intent(this, ChatActivity.class);
            String money = null, words = null;

            //根据Tab的Item来判断当前发送的是那种红包
            final int item = viewPager.getCurrentItem();

            //获取金额和文字信息(口令或者祝福语)
            if (item == 0) {
                money = editTextPt.getText().toString();
                words = editTextGre.getText().toString();
                if (StringUtils.isNullOrEmpty(words)) {
                    words = editTextGre.getHint().toString();
                }
            } else if (item == 1) {
                money = editTextKl.getText().toString();
                words = editTextPwd.getText().toString();
                if (StringUtils.isNullOrEmpty(words)) {
                    words = editTextPwd.getHint().toString();
                    words = words.substring(1, words.length());
                }
            }
            if (StringUtils.isNullOrEmpty(money)) {
                ToastUtil.showToast(mContext, InternationalizationHelper.getString("JX_InputGiftCount"));
            } else if (Double.parseDouble(money) > Double.parseDouble(coreManager.getConfig().maxSendRedPagesAmount) || Double.parseDouble(money) <= 0) {
//                ToastUtil.showToast(mContext, InternationalizationHelper.getString("JXRechargeVC_MoneyCount"));
                ToastUtil.showToast(mContext, String.format(getString(R.string.red_packet_range), coreManager.getConfig().maxSendRedPagesAmount));
            } else if (Double.parseDouble(money) > coreManager.getSelf().getBalance()) {
                ToastUtil.showToast(mContext, InternationalizationHelper.getString("JX_NotEnough"));
            } else {
                PayPasswordVerifyDialog dialog = new PayPasswordVerifyDialog(this);
                dialog.setAction(getString(R.string.chat_redpacket));
                dialog.setMoney(money);
                final String finalMoney = money;
                final String finalWords = words;
                dialog.setOnInputFinishListener(password -> {
                    // 回传信息
                    bundle.putString("money", finalMoney);
                    bundle.putString(item == 0 ? "greetings" : "password", finalWords);
                    bundle.putString("type", item == 0 ? "1" : "3"); // 类型
                    bundle.putString("count", "1"); // 因为是单聊，所以个数必须是一
                    bundle.putString("payPassword", password);
                    data.putExtras(bundle);
                    setResult(item == 0 ? ChatActivity.REQUEST_CODE_SEND_RED_PT : ChatActivity.REQUEST_CODE_SEND_RED_KL, data);
                    finish();
                });
                dialog.show();
            }
        } else {
            // 根据Tab按钮传递的Tag来判断是那个页面，设置到相应的界面并且去掉动画
            int index = Integer.parseInt(v.getTag().toString());
            viewPager.setCurrentItem(index, false);
        }
    }

    private class PagerAdapter extends android.support.v4.view.PagerAdapter {

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(View container, int position) {
            ((ViewGroup) container).addView(views.get(position));
            return views.get(position);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return mTitleList.get(position);
        }
    }
}
