package com.gwh.customview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.gwh.customview.R;

/**
 * Created by JoJo on 2018/8/14.
 * wechat:18510829974
 * description:自定义ViewPager
 */

public class MyViewPager extends ViewGroup {
    private static final String TAG = "MyViewPager";
    private Context mContext;
    private int[] images = {R.mipmap.a, R.mipmap.b, R.mipmap.c};
    private GestureDetector mGestureDetector;
    private Scroller mScroller;
    private int position;

    private int scrollX;
    private int startX;
    private int startY;

    public MyViewPager(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public MyViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    private void init() {
//        for (int i = 0; i < images.length; i++) {
//            ImageView iv = new ImageView(getContext());
//            iv.setBackgroundResource(images[i]);
//            this.addView(iv);
//        }
//        //由于ViewGroup默认只测量下面一层的子View(所以我们直接在ViewGroup里面添加ImageView是可以直接显示出来的)，所以基本自定义ViewGroup都会要重写onMeasure方法，否则无法测量第一层View（这里是ScrollView）中的view，无法正常显示里面的内容。
//        View testView = View.inflate(mContext, R.layout.test_viewpager_scrollview, null);
//        addView(testView, 2);

        mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                //相对滑动：X方向滑动多少距离，view就跟着滑动多少距离
                scrollBy((int) distanceX, 0);
                Log.e(TAG, "onScroll  " + distanceX);
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });
        mScroller = new Scroller(mContext);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            //如果是view:触发view的测量;如果是ViewGroup，触发测量ViewGroup中的子view
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 如果左右滑动, 就需要拦截, 上下滑动,不需要拦截
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = (int) ev.getX();
                startY = (int) ev.getY();
                mGestureDetector.onTouchEvent(ev);
                Log.e(TAG, "onInterceptTouchEvent  ACTION_DOWN");
//                return true;
                break;
            case MotionEvent.ACTION_MOVE:
                int endX = (int) ev.getX();
                int endY = (int) ev.getY();
                int dx = endX - startX;
                int dy = endY - startY;
                Log.e(TAG, "onInterceptTouchEvent  ACTION_MOVE  " + dx + "  " + dy);
                if (Math.abs(dx) > Math.abs(dy)) {
                    // 左右滑动
                    return true;// 中断事件传递, 不允许孩子响应事件了, 由父控件处理
                }
                break;
            default:
                break;
        }
        return false;// 不拦截事件,优先传递给孩子(也就是ScrollView，让它正常处理上下滑动事件)处理

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //将触摸事件传递手势识别器
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG, "onTouchEvent  ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                scrollX = getScrollX();//相对于初始位置滑动的距离
                //你滑动的距离加上屏幕的一半，除以屏幕宽度，就是当前图片显示的pos.如果你滑动距离超过了屏幕的一半，这个pos就加1
                position = (getScrollX() + getWidth() / 2) / getWidth();
                //屏蔽边界值：postion在0~images.length-1范围内
                if (position >= images.length) {
                    position = images.length - 1 + 1;
                }
                if (position < 0) {
                    position = 0;
                }

                if (mOnPageScrollListener != null) {
                    Log.e("TAG", "offset=" + (float) (getScrollX() * 1.0 / ((1) * getWidth())));
                    mOnPageScrollListener.onPageScrolled((float) (getScrollX() * 1.0 / (getWidth())), position);
                }
                Log.e(TAG, "onTouchEvent ACTION_MOVE =" + scrollX + "  位置： " + position);
                break;
            case MotionEvent.ACTION_UP:

                //绝对滑动，直接滑到指定的x,y的位置,较迟钝
//                scrollTo(position * getWidth(), 0);
//                Log.e("TAG", "水平方向回弹滑动的距离=" + (-(scrollX - position * getWidth())));
                //滚动，startX, startY为开始滚动的位置，dx,dy为滚动的偏移量
                mScroller.startScroll(scrollX, 0, -(scrollX - position * getWidth()), 0);
                invalidate();//使用invalidate这个方法会有执行一个回调方法computeScroll，我们来重写这个方法

                if (mOnPageScrollListener != null) {
                    mOnPageScrollListener.onPageSelected(position);
                }
                Log.e(TAG, "onTouchEvent ACTION_UP ");
                break;
        }
        return true;
    }

    /**
     * 其实Scroller的原理就是用ScrollTo来一段一段的进行，最后看上去跟自然的一样，必须使用postInvalidate，
     * 这样才会一直回调computeScroll这个方法，直到滑动结束。
     */
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            Log.e(TAG, "computeScroll  " + mScroller.getCurrX());
            postInvalidate();
            if (mOnPageScrollListener != null) {
                Log.e(TAG, "offset=" + (float) (getScrollX() * 1.0 / (getWidth())));
                mOnPageScrollListener.onPageScrolled((float) (mScroller.getCurrX() * 1.0 / ((1) * getWidth())), position);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            childView.layout(i * getWidth(), t, (i + 1) * getWidth(), b);

        }
    }

    public interface OnPageScrollListener {
        /**
         * @param offsetPercent offsetPercent：getScrollX滑动的距离占屏幕宽度的百分比
         * @param position
         */
        void onPageScrolled(float offsetPercent, int position);

        void onPageSelected(int position);
    }

    private OnPageScrollListener mOnPageScrollListener;

    public void setOnPageScrollListener(OnPageScrollListener onPageScrollListener) {
        this.mOnPageScrollListener = onPageScrollListener;
    }
}