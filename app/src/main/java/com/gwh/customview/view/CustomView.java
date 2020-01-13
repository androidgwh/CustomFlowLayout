package com.gwh.customview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;

import androidx.annotation.Nullable;

public class CustomView extends LinearLayout {
    private static final String TAG = "CustomView";
    private VelocityTracker obtain;
    private GestureDetector gesture;
    private Scroller scroller;
    private float mLastX;
    private float mLastY;

    public CustomView(Context context) {
        super(context);
        init(context);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 初始化速度追踪器
     */
    private void init(Context context) {
        obtain = VelocityTracker.obtain();
        //辅助检测用户的单机、滑动、长按、双击等行为
        gesture = new GestureDetector(context, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return false;
            }
        });
        gesture.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent motionEvent) {
                return false;
            }
        });
        scroller = new Scroller(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != gesture) {
            gesture.onTouchEvent(event);
        }
        if (null != obtain) {
            //必须操作，把当前操作加入追踪器
            obtain.addMovement(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getX();
                mLastY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int offX = (int) (event.getX() - mLastX);
                int offY = (int) (event.getY() - mLastY);
                //两种方式实现view随手指移动
                //1、layout 重新布局
//                layout(getLeft() + offX, getTop() + offY, getRight() + offX, getBottom() + offY);
                //2、setX setY
                setX(getX() + offX);
                setY(getY() + offY);
                if (null != obtain) {
                    //先计算
                    obtain.computeCurrentVelocity(1000);
                    Log.d(TAG, "getXVelocity  :" + obtain.getXVelocity() + " getYVelocity : " + obtain.getYVelocity());
                }
                break;
            default:
                break;
        }

        return true;
    }

//    public void smoothScroolTo(int destX,int destY){
//        int scrollX = scrollY;
//        int delta = destY - scrollY;
//        //2000 ms 内滑动到 destX 位置，效果就是缓慢滑动
//        scroller.startScroll(scrollX, 0, 0, delta, 2000);
//        invalidate();
//
//
//    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);

    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }

    }

    /**
     * 销毁
     */
    public void onDestory() {
        if (null != obtain) {
            obtain.clear();
            obtain.recycle();
            obtain = null;
        }
    }
}
