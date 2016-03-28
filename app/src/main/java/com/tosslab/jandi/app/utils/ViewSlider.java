package com.tosslab.jandi.app.utils;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by tonyjs on 16. 1. 28..
 */
public class ViewSlider extends RecyclerView.OnScrollListener {

    private boolean isInitialized = true;
    private View targetView;
    public ViewSlider(View targetView) {
        this.targetView = targetView;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (targetView == null) {
            return;
        }

        if (isInitialized) {
            isInitialized = false;
            return;
        }

        int recyclerViewMeasuredHeight = recyclerView.getMeasuredHeight();
        int targetViewMeasuredHeight = targetView.getMeasuredHeight();
        int childCount = recyclerView.getChildCount();
        if (recyclerViewMeasuredHeight <= 0
                || targetViewMeasuredHeight <= 0
                || childCount <= 0) {
            return;
        }

        final float translateY = targetView.getTranslationY() - dy;

        float futureTranslateY = Math.max(-targetView.getMeasuredHeight(), translateY);
        targetView.setTranslationY(Math.min(0, futureTranslateY));
    }
}
