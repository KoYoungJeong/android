package com.tosslab.jandi.app.views.viewgroup;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Steve SeongUg Jung on 15. 8. 12..
 */
public class AdvancedViewPager extends ViewPager {
    public AdvancedViewPager(Context context) {
        super(context);
    }

    public AdvancedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
