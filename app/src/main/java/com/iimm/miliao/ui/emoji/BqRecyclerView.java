package com.iimm.miliao.ui.emoji;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.List;

/**
 * Created by shichaohui on 2015/7/9 0009.
 * <p>
 * 横向分页的GridView效果
 * </p>
 * <p>
 * 默认为1行，每页3列，如果要自定义行数和列数，请在调用{@link BqRecyclerView#setAdapter(Adapter)}方法前调用
 * {@link BqRecyclerView#setPageSize(int, int)}方法自定义行数
 * </p>
 */
public class BqRecyclerView extends RecyclerView {

    private Context mContext = null;

    private PageAdapter myAdapter = null;

    private int shortestDistance; // 超过此距离的滑动才有效
    private int minMove = 3; // 超过此距离的滑动才有效
    private float downX = 0; // 手指按下的X轴坐标
    private float slideDistance = 0; // 滑动的距离
    private float scrollX = 0; // X轴当前的位置

    private int spanRow = 1; // 行数
    private int spanColumn = 3; // 每页列数
    private int totalPage = 0; // 总页数
    private int currentPage = 1; // 当前页

    private int pageMargin = 0; // 页间距

    private boolean isRecordPressingCoordinates = true;
    private PageIndicatorView mIndicatorView = null; // 指示器布局
    private float downX_dispatch;
    private boolean isDispatch = true;

    public BqRecyclerView(Context context) {
        this(context, null);
    }

    public BqRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BqRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        defaultInit(context);
    }

    // 默认初始化
    private void defaultInit(Context context) {
        this.mContext = context;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(
                mContext, spanRow, GridLayoutManager.HORIZONTAL, false);
        setLayoutManager(gridLayoutManager);
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    /**
     * 设置行数和每页列数
     *
     * @param spanRow    行数，<=0表示使用默认的行数
     * @param spanColumn 每页列数，<=0表示使用默认每页列数
     */
    public void setPageSize(int spanRow, int spanColumn) {
        this.spanRow = spanRow <= 0 ? this.spanRow : spanRow;
        this.spanColumn = spanColumn <= 0 ? this.spanColumn : spanColumn;
        setLayoutManager(new GridLayoutManager(
                mContext, this.spanRow, GridLayoutManager.HORIZONTAL, false));
    }

    /**
     * 设置页间距
     *
     * @param pageMargin 间距(px)
     */
    public void setPageMargin(int pageMargin) {
        this.pageMargin = pageMargin;
    }

    /**
     * 设置指示器
     *
     * @param indicatorView 指示器布局
     */
    public void setIndicator(PageIndicatorView indicatorView) {
        this.mIndicatorView = indicatorView;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        shortestDistance = getMeasuredWidth() / 4;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        this.myAdapter = (PageAdapter) adapter;
        update();
    }

    // 更新页码指示器和相关数据
    private void update() {
        // 计算总页数
        int temp = ((int) Math.ceil(myAdapter.dataList.size() / (double) (spanRow * spanColumn)));
        if (temp != totalPage) {
            mIndicatorView.initIndicator(temp);
            // 页码减少且当前页为最后一页
            if (temp < totalPage && currentPage == totalPage) {
                currentPage = temp;
                // 执行滚动
                smoothScrollBy(-getWidth(), 0);
            }
            mIndicatorView.setSelectedPage(currentPage - 1);
            totalPage = temp;
        }
    }
//
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                downX_dispatch = ev.getX();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if (currentPage != totalPage && currentPage != 1) {
//                    return true;
//                } else if (currentPage == totalPage && ev.getX() - downX_dispatch < 0) {
//                    return false;
//                } else if (currentPage == 1 && ev.getX() - downX_dispatch > 0) {
//                    return false;
//                }else{
//                    return super.dispatchTouchEvent(ev);
//                }
//            case MotionEvent.ACTION_UP:
//                break;
//        }
//        return super.dispatchTouchEvent(ev);
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
//                if (currentPage == totalPage && event.getX() - downX < 0) {
//                    return false;
//                }
//                if (currentPage == 1 && event.getX() - downX < 0) {
//                    return false;
//                }
//                if (currentPage == totalPage && downX - event.getX() > 0) {
//                    return false;
//                }
                if (isRecordPressingCoordinates) {
                    isRecordPressingCoordinates = false;
                    downX = event.getX();
                }
                break;
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                slideDistance = event.getX() - downX;
                if (Math.abs(slideDistance) <= minMove) {
                    return super.onTouchEvent(event);
                }
                if (Math.abs(slideDistance) > shortestDistance) {
                    // 滑动距离足够，执行翻页
                    if (slideDistance > 0) {
                        currentPage = currentPage == 1 ? 1 : currentPage - 1;
                    } else {
                        currentPage = currentPage == totalPage ? totalPage : currentPage + 1;
                    }
                    // 修改指示器选中项
                    mIndicatorView.setSelectedPage(currentPage - 1);
                }
                // 执行滚动
                smoothScrollBy((int) ((currentPage - 1) * getWidth() - scrollX), 0);
                isRecordPressingCoordinates = true;
                return true;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // interceptTouch是自定义属性控制是否拦截事件

            ViewParent parent =this;
            // 循环查找ViewPager, 请求ViewPager不拦截触摸事件
            while(!((parent = parent.getParent()) instanceof ViewPager)){
                // nop
            }

            parent.requestDisallowInterceptTouchEvent(true);


        return super.dispatchTouchEvent(ev);

    }
    @Override
    public void onScrolled(int dx, int dy) {
        scrollX += dx;
        super.onScrolled(dx, dy);
    }

    public void initCurrentPage() {
        currentPage = 1;
        scrollX = 0;
    }

    /**
     * 数据适配器
     */
    public class PageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<?> dataList = null;
        private CallBack mCallBack = null;
        private int itemWidth = 0;
        private int itemCount = 0;

        /**
         * 实例化适配器
         *
         * @param data
         * @param callBack
         */
        public PageAdapter(List<?> data, CallBack callBack) {
            this.dataList = data;
            this.mCallBack = callBack;
            int pageSize = spanRow * spanColumn;
            int temp = data.size() % pageSize;
            if (temp != 0) {
                itemCount = dataList.size() + (spanRow * spanColumn - temp);
            } else {
                itemCount = dataList.size();
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (itemWidth <= 0) {
                // 计算Item的宽度
                itemWidth = (parent.getWidth() - pageMargin * 2) / spanColumn;
            }

            RecyclerView.ViewHolder holder = mCallBack.onCreateViewHolder(parent, viewType);

            holder.itemView.measure(0, 0);
            holder.itemView.getLayoutParams().width = itemWidth;
            //   holder.itemView.getLayoutParams().height = holder.itemView.getMeasuredHeight();

            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (spanColumn == 1) {
                // 每个Item距离左右两侧各pageMargin
                holder.itemView.getLayoutParams().width = itemWidth + pageMargin * 2;
                holder.itemView.setPadding(pageMargin, 0, pageMargin, 0);
            } else {
                int m = position % (spanRow * spanColumn);
                if (m < spanRow) {
                    // 每页左侧的Item距离左边pageMargin
                    holder.itemView.getLayoutParams().width = itemWidth + pageMargin;
                    holder.itemView.setPadding(pageMargin, 0, 0, 0);
                } else if (m >= spanRow * spanColumn - spanRow) {
                    // 每页右侧的Item距离右边pageMargin
                    holder.itemView.getLayoutParams().width = itemWidth + pageMargin;
                    holder.itemView.setPadding(0, 0, pageMargin, 0);
                } else {
                    // 中间的正常显示
                    holder.itemView.getLayoutParams().width = itemWidth;
                    holder.itemView.setPadding(0, 0, 0, 0);
                }
            }

            //  if (position < dataList.size()) {
            holder.itemView.setVisibility(View.VISIBLE);
            mCallBack.onBindViewHolder(holder, position);
            //  } else {
//                if ((position + 1) % (spanRow*spanColumn) == 0) {
//                    holder.itemView.setVisibility(View.GONE);
//                    mCallBack.onLastBindViewHolder(holder, position);
//                }else{
//                    holder.itemView.setVisibility(View.GONE);
//                }
//            }

        }

        @Override
        public int getItemCount() {
            return itemCount;
        }

        /**
         * 删除Item
         *
         * @param position 位置
         */
        public void remove(int position) {
            if (position < dataList.size()) {
                // 删除数据
                dataList.remove(position);
                itemCount--;
                // 删除Item
                notifyItemRemoved(position);
                // 更新界面上发生改变的Item
                notifyItemRangeChanged(position, currentPage * spanRow * spanColumn);
                // 更新页码指示器
                update();
            }
        }

    }

    public interface CallBack {

        /**
         * 创建VieHolder
         *
         * @param parent
         * @param viewType
         */
        RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

        /**
         * 绑定数据到ViewHolder
         *
         * @param holder
         * @param position
         */
        void onBindViewHolder(RecyclerView.ViewHolder holder, int position);

    }

}
