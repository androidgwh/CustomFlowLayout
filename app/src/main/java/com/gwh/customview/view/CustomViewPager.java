package com.gwh.customview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 步骤
 * <p>
 * 1、创建 Scroller 的实例
 * 2、调用 startScroll() 方法来初始化滚动数据并刷新界面
 * 3、重写 computeScroll() 方法，并在其内部完成平滑滚动的逻辑
 */
public class CustomViewPager extends ViewGroup {
    private static final String TAG = "CustomViewPager";
    private Scroller mScroller;

    //左边界
    private int mLeftBorder;
    //右边界
    private int mRightBorder;

    private float mLastX;
    private float mLastMoveX;
    private float mMoveX;

    //最小滑动的px
    private int scaledPagingTouchSlop;
    private GestureDetector mGestureDetector;

    public CustomViewPager(Context context) {
        super(context);
        init(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mScroller = new Scroller(context);
        //确认手指滑动的最小px
        scaledPagingTouchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
        mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                //相对滑动：X方向滑动多少距离，view就跟着滑动多少距离
                scrollBy((int) distanceX, 0);
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }

    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        if (b) {
            for (int j = 0; j < getChildCount(); j++) {
                View childAt = getChildAt(i);
                childAt.layout(i * childAt.getMeasuredWidth(), 0, (i + 1) * childAt.getMeasuredWidth(), childAt.getMeasuredHeight());
            }
            //初始化左右边界
            mLeftBorder = getChildAt(0).getLeft();
            mRightBorder = getChildAt(getChildCount() - 1).getRight();
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        //处理平滑滚动逻辑
        if (null != mScroller && mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                mLastX = ev.getX();
//                mLastMoveX = mLastX;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                mMoveX = ev.getX();
//                float offX = Math.abs(mMoveX - mLastX);
//                mLastMoveX = mMoveX;
//                if (offX > scaledPagingTouchSlop) {
//                    //认为子控件在滑动，拦截子控件的触摸事件
//                    return true;
//                }
//                break;
//        }
//        return super.onInterceptTouchEvent(ev);
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mMoveX = event.getRawX();
                float scrollX = (mLastMoveX - mMoveX);
//                if (getScrollX() + scrollX < mLeftBorder) {
//                    scrollTo(mLeftBorder, 0);
//                    return true;
//                } else if (getScrollX() + getWidth() + scrollX > mRightBorder) {
//                    scrollTo(mRightBorder - getWidth(), 0);
//                    return true;
//                }
//                scrollBy((int) scrollX, 0);
                break;
            case MotionEvent.ACTION_UP:
                int targetIndex = (getScrollX() + getWidth() / 2) / getWidth();
                int dx = targetIndex * getWidth() - getScrollX();
                Log.d(TAG, targetIndex + "  " + dx);
                mScroller.startScroll(getScrollX(), 0, dx, 0);
                invalidate();
                break;
        }
        return true;
    }


}
