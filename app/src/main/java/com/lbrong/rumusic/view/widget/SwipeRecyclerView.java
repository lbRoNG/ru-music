package com.lbrong.rumusic.view.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;

/**
 * @author lbRoNG
 * @since 2018/8/21
 */
public class SwipeRecyclerView extends RecyclerView {
    private boolean interceptTouch;

    public SwipeRecyclerView(Context context) {
        super(context);
    }

    public SwipeRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
    }

    public void setInterceptTouch(boolean interceptTouch) {
        this.interceptTouch = interceptTouch;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        // interceptTouch是自定义属性控制是否拦截事件
        if (interceptTouch){
            ViewParent parent =this;
            // 循环查找ViewPager, 请求ViewPager不拦截触摸事件
            while(!((parent = parent.getParent()) instanceof ViewPager)){
                // nop
            }
            parent.requestDisallowInterceptTouchEvent(true);
        }
        return super.dispatchTouchEvent(e);
    }
}
