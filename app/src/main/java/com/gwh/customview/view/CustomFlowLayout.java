package com.gwh.customview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义的流式布局
 * <p>
 * 分析：
 * 1、指定LayoutParams，需要识别margin
 * 2、根据子view的宽和高，计算自己的宽和高
 */
public class CustomFlowLayout extends ViewGroup {
    private static final String TAG = CustomFlowLayout.class.getSimpleName();

    /**
     * 存储所有的view，按行记录
     */
    private List<List<View>> mViews = new ArrayList<>();
    /**
     * 存储每一行的最大高度
     */
    private List<Integer> mLineHeight = new ArrayList<>();

    public CustomFlowLayout(Context context) {
        super(context);
    }

    public CustomFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 需要支持margin，使用系统的MarginLayoutParams
     *
     * @param p
     * @return
     */
//    @Override
//    protected LayoutParams generateLayoutParams(LayoutParams p) {
//        return new MarginLayoutParams(p);
//    }
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    /**
     * 设置子空间的测量模式和大小，根据所有子控件设置自己的宽高
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取父容器为它设置的测量模式和大小
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        Log.d(TAG, "sizeWidth :" + sizeWidth + " modeWidth:  " + modeWidth + "\nsizeHeight:  " + sizeHeight + " modeHeight: " + modeHeight);
        //如果是wrap_content，记录宽和高
        int width = 0;
        int height = 0;
        //每一行的宽度，不断取最大宽度
        int lineWidth = 0;
        //每一行的高度，累加至height
        int lineHeight = 0;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            //测量每一个view的宽和高
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
            //得到每一个view的LayoutParams
            MarginLayoutParams layoutParams = (MarginLayoutParams) childView.getLayoutParams();
            //得到每一个childView的实际宽高，包括margin
            int childWidth = childView.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
            int childHeight = childView.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
            /**
             * 如果加入目前child的宽度超出最大宽度，则给到目前最大宽度给width，累加height,然后开启新行即可
             */
            if (lineWidth + childWidth > sizeWidth) {
                width = Math.max(lineWidth, childWidth);
                //重新开启新行
                lineWidth = childWidth;
                //累加当前高度
                height += lineHeight;
                //开启下一行的高度
                lineHeight = childHeight;
            } else {
                /**
                 * 否则lineWidth累加，lineHeight取最大值
                 */
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
            /**
             * 最后一个，取当前记录的最大宽度和lineWidth作比较
             */
            if (i == childCount - 1) {
                width = Math.max(width, lineWidth);
                height += lineHeight;
            }
            Log.d(TAG, "width : " + width + "  height: " + height);
        }
        /**
         * 如果该viewGroup设置为wrap_content，则由全部子view的宽高margin计算出的宽高，得出该viewGroup
         * 的宽和高，否则就使用该viewGroup指定的宽和高
         * */

        setMeasuredDimension(modeWidth == MeasureSpec.EXACTLY ? sizeWidth : width,
                modeHeight == MeasureSpec.EXACTLY ? sizeHeight : height);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mViews.clear();
        mLineHeight.clear();
        int childCount = getChildCount();
        int width = getWidth();
        int lineHeight = 0;
        int lineWidth = 0;
        List<View> lineViews = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            MarginLayoutParams layoutParams = (MarginLayoutParams) childView.getLayoutParams();
            int childWidth = childView.getMeasuredWidth();
            int childHeight = childView.getMeasuredHeight();
            if (childWidth + layoutParams.leftMargin + layoutParams.rightMargin + lineWidth
                    > width) {
                //需要换行
                mLineHeight.add(lineHeight);
                mViews.add(lineViews);
                lineWidth = 0;
                lineViews = new ArrayList<>();
            }
            //不需要换行，累加宽度，高度取最大值
            lineWidth += childWidth + layoutParams.leftMargin + layoutParams.rightMargin;
            lineViews.add(childView);
            lineHeight = Math.max(lineHeight, childHeight + layoutParams.topMargin + layoutParams.bottomMargin);

        }
        //记录最后一行
        mLineHeight.add(lineHeight);
        mViews.add(lineViews);

        int left = 0;
        int top = 0;
        //总行数
        int viewLines = mViews.size();
        for (int i = 0; i < viewLines; i++) {
            //每一行的所有view
            List<View> views = mViews.get(i);
            //每一行的高度
            lineHeight = mLineHeight.get(i);
            if (null != views && !views.isEmpty()) {
                for (int j = 0; j < views.size(); j++) {
                    View childView = views.get(j);
                    if (childView.getVisibility() == GONE) {
                        return;
                    }
                    MarginLayoutParams layoutParams = (MarginLayoutParams) childView.getLayoutParams();
                    //计算childView的left top right bottom
                    int childLeft = left + layoutParams.leftMargin;
                    int childTop = top + layoutParams.topMargin;
                    int childRight = childLeft + childView.getMeasuredWidth();
                    int childBottom = childTop + childView.getMeasuredHeight();
                    childView.layout(childLeft, childTop, childRight, childBottom);
                    left += childView.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
                }
                //下一行
                left = 0;
                top += lineHeight;
            }

        }

    }
}
