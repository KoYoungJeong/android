package com.tosslab.jandi.app.ui.share.model;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

/**
 * Created by jsuch2362 on 15. 11. 17..
 */
public class ScrollViewHelper {

    private View view;
    private ScrollView scrollView;

    private int viewFirstScrollY;
    private float lastTouchY;

    public ScrollViewHelper(View view, ScrollView scrollView) {
        this.view = view;
        this.scrollView = scrollView;

        initTouchMode();
    }

    public void initTouchMode() {
        view.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                viewFirstScrollY = view.getScrollY();
                lastTouchY = event.getRawY();
                scrollView.requestDisallowInterceptTouchEvent(true);
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (viewFirstScrollY != 0) {
                    // 첫 터치의 스크롤 지점이 0이 아닌 경우..
                    if (v.getScrollY() > 0) {
                        scrollView.requestDisallowInterceptTouchEvent(true);
                    } else {
                        scrollView.requestDisallowInterceptTouchEvent(false);
                    }
                } else {
                    // 첫 터치시 스크롤지점이 0이면 0을 벗어날때까지는 아무런 제어하지 않음
                    int currentScrollY = view.getScrollY();
                    if (currentScrollY > 0) {
                        viewFirstScrollY = currentScrollY;
                    } else if (lastTouchY < event.getRawY()){
                        // 첫 스크롤지점 = 0, 현재 스크롤지점 = 0, 하향 스크롤이면 터치 제어권 넘김
                        scrollView.requestDisallowInterceptTouchEvent(false);
                    }
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                scrollView.requestDisallowInterceptTouchEvent(false);
            }

            return false;
        });
    }


}
