package com.iimm.miliao.bean;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * @author yiw
 * @ClassName: ActionItem
 * @Description: 弹窗内部子类项（绘制标题和图标）
 * @date 2015-12-28 下午3:43:30
 */
public class ActionItem {
    // 定义图片对象
    public Drawable mDrawable;
    // 定义文本对象
    public CharSequence mTitle;

    public boolean mSelected;

    public ActionItem(Drawable drawable, CharSequence title) {
        this.mDrawable = drawable;
        this.mTitle = title;
    }

    public ActionItem(CharSequence title) {
        this.mDrawable = null;
        this.mTitle = title;
    }

    public ActionItem(Context context, int titleId, int drawableId) {
        this.mTitle = context.getResources().getText(titleId);
        this.mDrawable = context.getResources().getDrawable(drawableId);
    }

    public ActionItem(Context context, CharSequence title, int drawableId) {
        this.mTitle = title;
        this.mDrawable = context.getResources().getDrawable(drawableId);
    }

    public void setItemTv(CharSequence tv) {
        mTitle = tv;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }
}
