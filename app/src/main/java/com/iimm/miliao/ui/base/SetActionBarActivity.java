package com.iimm.miliao.ui.base;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.iimm.miliao.R;
import com.iimm.miliao.util.SkinUtils;
import com.iimm.miliao.util.StatusBarUtil;

/**
 * Created by Administrator on 2016/4/20.
 */
public abstract class SetActionBarActivity extends DefaultResourceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setStatusBarColor();
        super.onCreate(savedInstanceState);
        setActionBar();
    }

    protected void setActionBar() {
        if (getSupportActionBar() != null) { // 因为有的activity没有actionBar，所以加个判断
            int skinColor;
            int colorName = SkinUtils.getSkin(this).getColorName();
            if (colorName == R.string.skin_simple_white) {
                skinColor = ContextCompat.getColor(this, R.color.tb_bg_skin_simple_white);
            } else {
                skinColor = SkinUtils.getSkin(this).getPrimaryColor();
            }
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(skinColor));
            if (Build.VERSION.SDK_INT >= 21) {//兼容5.0  去除actionbar阴影   //草  我说的 我设置 阴影一直不起做用。。。
                getSupportActionBar().setElevation(0);
            }
        }
    }

    /**
     * 沉浸式状态栏
     */
    private void setStatusBarColor() {
        StatusBarUtil.setStatusBar(this, SkinUtils.getSkin(this).getPrimaryColor());
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            // >=5.0 背景为全透明
//            /* >=5.0，this method(getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS));
//            in some phone is half-transparent like vivo、nexus6p..
//            in some phone is full-transparent
//            so ...*/
//
////            Window window = getWindow();
////            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
////            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
////                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
////            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
////            window.setStatusBarColor(SkinUtils.getSkin(this).getPrimaryColor());
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            // 4.4背景为渐变半透明
//           getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }
    }
}
