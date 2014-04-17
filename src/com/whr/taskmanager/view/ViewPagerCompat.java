package com.whr.taskmanager.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ViewPagerCompat extends ViewPager {

	public ViewPagerCompat(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return super.onInterceptTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return super.onTouchEvent(ev);
	}

	@Override
	public boolean arrowScroll(int direction) {
		return super.arrowScroll(direction);
	}

}
