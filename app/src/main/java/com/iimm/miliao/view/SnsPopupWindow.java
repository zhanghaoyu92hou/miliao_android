package com.iimm.miliao.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.iimm.miliao.R;
import com.iimm.miliao.bean.ActionItem;
import com.iimm.miliao.util.DisplayUtil;
import com.iimm.miliao.util.ScreenUtil;

import java.util.ArrayList;

/**
 * 朋友圈点赞评论的popupwindow
 *
 * @author wei.yi
 */
public class SnsPopupWindow extends PopupWindow implements OnClickListener {

    private TextView digBtn;
    private TextView commentBtn;
    private LinearLayout ll_zan;
    private LinearLayout ll_pinlun;
    private LinearLayout ll_ivCollection;
    private LinearLayout ll_ivReport;
    private CheckableImageView ivAwesome;
    private CheckableImageView ivComment;
    private CheckableImageView ivCollection;


    // 实例化一个矩形
    private Rect mRect = new Rect();
    // 坐标的位置（x、y）
    private final int[] mLocation = new int[2];
    // 弹窗子类项选中时的监听
    private OnItemClickListener mItemClickListener;
    // 定义弹窗子类项列表
    private ArrayList<ActionItem> mActionItems = new ArrayList<ActionItem>();

    public void setmItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public CheckableImageView getmAwesome() {
        return ivAwesome;
    }

    public CheckableImageView getCollection() {
        return ivCollection;
    }

    public ArrayList<ActionItem> getmActionItems() {
        return mActionItems;
    }

    public void setmActionItems(ArrayList<ActionItem> mActionItems) {
        this.mActionItems = mActionItems;
    }

    public SnsPopupWindow(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.social_sns_popupwindow, null);
        digBtn = (TextView) view.findViewById(R.id.digBtn);
        commentBtn = (TextView) view.findViewById(R.id.commentBtn);
        ivAwesome = view.findViewById(R.id.ivAwesome);
        ivComment = view.findViewById(R.id.ivComment);
        ivCollection = view.findViewById(R.id.ivCollection);
	/*	digBtn.setOnClickListener(this);
		commentBtn.setOnClickListener(this);*/

        ll_zan = view.findViewById(R.id.ll_zan);
        ll_pinlun = view.findViewById(R.id.ll_pinlun);
        ll_ivCollection = view.findViewById(R.id.ll_ivCollection);
        ll_ivReport = view.findViewById(R.id.ll_ivReport);

        ll_zan.setOnClickListener(this);
        ll_pinlun.setOnClickListener(this);
        ll_ivCollection.setOnClickListener(this);
        ll_ivReport.setOnClickListener(this);


        this.setContentView(view);
        this.setWidth(DisplayUtil.dip2px(context, 150));
        this.setHeight(DisplayUtil.dip2px(context, 40));
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);
        this.setAnimationStyle(R.style.social_pop_anim);

        initItemData();
    }

    private void initItemData() {
        addAction(new ActionItem(""));
        addAction(new ActionItem(""));
        addAction(new ActionItem(""));
        addAction(new ActionItem(""));
    }

    public void showPopupWindow(View parent) {
        parent.getLocationOnScreen(mLocation);
        // 设置矩形的大小
        mRect.set(mLocation[0], mLocation[1], mLocation[0] + parent.getWidth(), mLocation[1] + parent.getHeight());
        digBtn.setText(mActionItems.get(0).mTitle);
        if (!this.isShowing()) {
            showAtLocation(parent, Gravity.NO_GRAVITY, mLocation[0] - this.getWidth() - ScreenUtil.dp2px(6)
                    , mLocation[1] - ((this.getHeight() - parent.getHeight()) / 2));
        } else {
            dismiss();
        }
    }

    @Override
    public void onClick(View view) {
        dismiss();
        switch (view.getId()) {
            case R.id.ll_zan:
                if (mItemClickListener != null)
                    mItemClickListener.onItemClick(mActionItems.get(0), 0);
                break;
            case R.id.ll_pinlun://评论
                if (mItemClickListener != null)
                    mItemClickListener.onItemClick(mActionItems.get(1), 1);
                break;
            case R.id.ll_ivCollection://收藏
                if (mItemClickListener != null)
                    mItemClickListener.onItemClick(mActionItems.get(2), 2);
                break;
            case R.id.ll_ivReport://举报
                if (mItemClickListener != null)
                    mItemClickListener.onItemClick(mActionItems.get(3), 3);
                break;
            default:
                break;
        }
    }

    /**
     * 添加子类项
     */
    public void addAction(ActionItem action) {
        if (action != null) {
            mActionItems.add(action);
        }
    }

    /**
     * 功能描述：弹窗子类项按钮监听事件
     */
    public static interface OnItemClickListener {
        public void onItemClick(ActionItem item, int position);
    }
}
