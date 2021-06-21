package com.tuacy.pinnedheader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * PinnedHeader对应的ItemDecoration
 */
public class PinnedHeaderItemDecoration extends RecyclerView.ItemDecoration implements IPinnedHeaderDecoration {

    /**
     * 分割线高度
     */
    private int mDividerSize;
    /**
     * 分割线画笔
     */
    private Paint mPaint;

    private Rect mPinnedHeaderRect = null;
    private int mPinnedHeaderPosition = -1;

    public PinnedHeaderItemDecoration(Context context) {
        this(context, 0.5F);
    }

    /**
     * 构造一个水平方向的透明颜色分割线
     *
     * @param dividerSize 分割线尺寸,单位dp
     */
    public PinnedHeaderItemDecoration(Context context, float dividerSize) {
        this(context, dividerSize, Color.TRANSPARENT);
    }

    /**
     * 使用颜色自定义分割线
     *
     * @param dividerSize  分割线尺寸,单位dp
     * @param dividerColor 分割线颜色
     */
    public PinnedHeaderItemDecoration(Context context, float dividerSize, @ColorInt int dividerColor) {
        mDividerSize = dip2px(context, dividerSize);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(dividerColor);
        mPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 把要固定的View绘制在上层
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        //确保是PinnedHeaderAdapter的adapter,确保有View
        if (!(parent.getAdapter() instanceof PinnedHeaderAdapter)
                || parent.getChildCount() <= 0) {
            return;
        }

        PinnedHeaderAdapter adapter = (PinnedHeaderAdapter) parent.getAdapter();
        //找到列表中的第一个itemView,这个itemView有可能是PinView,有可能是普通itemView
        View firstView = parent.getChildAt(0);
        int firstAdapterPosition = parent.getChildAdapterPosition(firstView);
        //找到第一个PinView在Adapter中的索引位置
        int pinnedHeaderPosition = getPinnedHeaderViewPosition(firstAdapterPosition, adapter);
        mPinnedHeaderPosition = pinnedHeaderPosition;

        if (pinnedHeaderPosition == -1) {
            mPinnedHeaderRect = null;
            return;
        }

        //创建PinView
        RecyclerView.ViewHolder pinnedHeaderViewHolder = adapter.onCreateViewHolder(parent,
                adapter.getItemViewType(pinnedHeaderPosition));

        //绑定PinView
        adapter.onBindViewHolder(pinnedHeaderViewHolder, pinnedHeaderPosition);
        //获取要固定的view
        View pinnedHeaderView = pinnedHeaderViewHolder.itemView;

        //1.将PinView测量后layout在顶部
        ensurePinnedHeaderViewLayout(pinnedHeaderView, parent);
        //布局完成后,获取到PinView的高度,用于计算吸顶PinView的偏移量
        int pinViewHeight = pinnedHeaderView.getHeight();

        //2.根据下一个PinView计算到顶部的PinView的偏移量
        int sectionPinOffset = 0;
        for (int index = 0; index < parent.getChildCount(); index++) {
            if (adapter.isPinnedPosition(parent.getChildAdapterPosition(parent.getChildAt(index)))) {
                View sectionView = parent.getChildAt(index);
                int sectionTop = sectionView.getTop();
                if (sectionTop < pinViewHeight && sectionTop > 0) {
                    //这个值肯定是<=0的
                    sectionPinOffset = sectionTop - pinViewHeight;
                }
                break;
            }
        }

        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) pinnedHeaderView.getLayoutParams();
        int saveCount = c.save();
        //sectionPinOffset<=0,所以canvas向上移动
        c.translate(layoutParams.leftMargin, sectionPinOffset);
        //裁剪掉pinnedHeaderView与当前的PinView上边重合的部分
        c.clipRect(0, 0, parent.getWidth(), pinnedHeaderView.getMeasuredHeight());
        //3.在顶部再绘制一次顶部PinView,覆盖在最列表中最上面的PinView上方,这个PinView是有偏移量的
        pinnedHeaderView.draw(c);
        c.restoreToCount(saveCount);

        if (mPinnedHeaderRect == null) {
            mPinnedHeaderRect = new Rect();
        }
        mPinnedHeaderRect.set(0, 0, parent.getWidth(), pinnedHeaderView.getMeasuredHeight() + sectionPinOffset);
    }

    /**
     * 要给每个item设置间距主要靠这个函数来实现
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getAdapter() instanceof PinnedHeaderAdapter) {
            int firstAdapterPosition = parent.getChildAdapterPosition(view);
            PinnedHeaderAdapter adapter = (PinnedHeaderAdapter) parent.getAdapter();

            //默认分割线是绘制在item的下方,当前要绘制的item是否是PinView
            boolean currentIsPinView = adapter.isPinnedPosition(firstAdapterPosition);
            //当前item的下方是否是PinView
            boolean nextIsPinView = firstAdapterPosition < adapter.getItemCount() - 1
                    && adapter.isPinnedPosition(firstAdapterPosition + 1);

            if (currentIsPinView || nextIsPinView) {
                outRect.set(0, 0, 0, 0);
                return;
            }
        }

        outRect.set(0, 0, 0, mDividerSize);
    }

    /**
     * 绘制分割线
     */
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        drawHorizontal(c, parent);
    }

    /**
     * 绘制横向分割线
     */
    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize - 1; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + layoutParams.bottomMargin;
            final int bottom = top + mDividerSize;
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }

    /**
     * 根据第一个可见的adapter的位置去获取临近的一个要固定的position的位置
     *
     * @param adapterFirstVisible 第一个可见的adapter的位置
     * @return -1：未找到 >=0 找到位置
     */
    private int getPinnedHeaderViewPosition(int adapterFirstVisible, PinnedHeaderAdapter adapter) {
        for (int index = adapterFirstVisible; index >= 0; index--) {
            if (adapter.isPinnedPosition(index)) {
                return index;
            }
        }
        return -1;
    }

    private void ensurePinnedHeaderViewLayout(View pinView, RecyclerView recyclerView) {
        if (pinView.isLayoutRequested()) {
            /**
             * 用的是RecyclerView的宽度测量，和RecyclerView的宽度一样
             */
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) pinView.getLayoutParams();
            if (layoutParams == null) {
                throw new NullPointerException("PinnedHeaderItemDecoration");
            }
            int widthSpec = View.MeasureSpec.makeMeasureSpec(
                    recyclerView.getMeasuredWidth() - layoutParams.leftMargin - layoutParams.rightMargin, View.MeasureSpec.EXACTLY);

            int heightSpec;
            if (layoutParams.height > 0) {
                heightSpec = View.MeasureSpec.makeMeasureSpec(layoutParams.height, View.MeasureSpec.EXACTLY);
            } else {
                heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            }
            pinView.measure(widthSpec, heightSpec);
            //固定到顶部
            pinView.layout(0, 0, pinView.getMeasuredWidth(), pinView.getMeasuredHeight());
        }
    }

    @Override
    public Rect getPinnedHeaderRect() {
        return mPinnedHeaderRect;
    }

    @Override
    public int getPinnedHeaderPosition() {
        return mPinnedHeaderPosition;
    }

    /**
     * dp2px
     */
    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
