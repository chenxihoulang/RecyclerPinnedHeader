package com.tuacy.recyclerpinnedheader.linear;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tuacy.recyclerpinnedheader.R;

/**
 * Created by chw on 2016/9/14 10:15.
 * RecycleView的分割线
 */
public class RecycleViewDivider extends RecyclerView.ItemDecoration {
    private Paint mPaint;
    private Drawable mDivider;
    private int mDividerSize = 1;//dp
    private int mOrientation;//列表的方向：LinearLayoutManager.VERTICAL或LinearLayoutManager.HORIZONTAL
    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

    /**
     * 默认分割线：高度为1dp,listDivider属性的样式
     *
     * @param orientation 列表方向:LinearLayoutManager.VERTICAL或LinearLayoutManager.HORIZONTAL
     */
    public RecycleViewDivider(Context context, int orientation) {
        if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
            throw new IllegalArgumentException("只能是水平或垂直方向");
        }
        mOrientation = orientation;

        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();

        mDividerSize = dip2px(context, 0.5f);
    }

    /**
     * 使用图片资源自定义分割线
     *
     * @param orientation 列表方向:LinearLayoutManager.VERTICAL或LinearLayoutManager.HORIZONTAL
     * @param drawableId  分割线图片资源
     */
    public RecycleViewDivider(Context context, int orientation, @DrawableRes int drawableId) {
        this(context, orientation);
        mDivider = ContextCompat.getDrawable(context, drawableId);
        mDividerSize = mDivider.getIntrinsicHeight();
    }

    /**
     * 构造一个水平方向的透明颜色分割线,线条高度值:0.5dp
     */
    public RecycleViewDivider(Context context) {
        this(context, 0.5f);
    }

    /**
     * 构造一个水平方向的透明颜色分割线
     *
     * @param dividerSize 分割线尺寸,单位dp
     */
    public RecycleViewDivider(Context context, float dividerSize) {
        this(context, LinearLayoutManager.HORIZONTAL, dividerSize,
                ContextCompat.getColor(context, R.color.transparent));
    }

    /**
     * 使用颜色自定义分割线
     *
     * @param orientation  列表方向:LinearLayoutManager.VERTICAL或LinearLayoutManager.HORIZONTAL
     * @param dividerSize  分割线尺寸,单位dp
     * @param dividerColor 分割线颜色
     */
    public RecycleViewDivider(Context context, int orientation, float dividerSize, @ColorInt int dividerColor) {
        this(context, orientation);
        mDividerSize = dip2px(context, dividerSize);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(dividerColor);
        mPaint.setStyle(Paint.Style.FILL);
    }


    //获取分割线尺寸
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            outRect.set(0, 0, mDividerSize, 0);
        } else {
            outRect.set(0, 0, 0, mDividerSize);
        }
    }

    //绘制分割线
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    //绘制横向 item 分割线
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
            } else if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }

        }
    }

    //绘制纵向 item 分割线
    private void drawVertical(Canvas canvas, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize - 1; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + layoutParams.rightMargin;
            final int right = left + mDividerSize;

            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            } else if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }

        }
    }

    /**
     * dp2px
     */
    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}