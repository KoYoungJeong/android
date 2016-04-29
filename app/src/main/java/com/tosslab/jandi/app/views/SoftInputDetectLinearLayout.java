package com.tosslab.jandi.app.views;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by tonyjs on 16. 2. 17..
 */
public class SoftInputDetectLinearLayout extends LinearLayout {
    private OnSoftInputDetectListener onSoftInputDetectListener;

    private SoftInputShowingStatus softInputShowingStatus = SoftInputShowingStatus.HIDE;
    private boolean shouldSkipSoftInputDetecting = false;
    private int currentOrientation;
    private int minDetectingSoftInputHeight;

    public SoftInputDetectLinearLayout(Context context) {
        super(context);
        init();
    }

    public SoftInputDetectLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SoftInputDetectLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        currentOrientation = getContext().getResources().getConfiguration().orientation;
        minDetectingSoftInputHeight =
                (int) (100 * getContext().getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (currentOrientation != newConfig.orientation) {
            currentOrientation = newConfig.orientation;
            shouldSkipSoftInputDetecting = true;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isInEditMode()
                || onSoftInputDetectListener == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        if (shouldSkipSoftInputDetecting) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            shouldSkipSoftInputDetecting = false;
            return;
        }

        int newHeight = MeasureSpec.getSize(heightMeasureSpec);
        int currentHeight = getHeight();

        // 뷰 사이즈를 다시 재기 전 높이와 다시 재어진 높이가 100dp 이상 차이나는 것을 기준으로
        // Soft Input 이 올라온지 내려간지를 가늠함.
        if (currentHeight - minDetectingSoftInputHeight > newHeight) {
            if (softInputShowingStatus != SoftInputShowingStatus.SHOW) {
                softInputShowingStatus = SoftInputShowingStatus.SHOW;
                onSoftInputDetectListener.onSoftInputDetected(true, currentHeight - newHeight);
            }
        } else if (currentHeight < newHeight - minDetectingSoftInputHeight) {
            if (softInputShowingStatus != SoftInputShowingStatus.HIDE) {
                softInputShowingStatus = SoftInputShowingStatus.HIDE;
                onSoftInputDetectListener.onSoftInputDetected(false, 0);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setOnSoftInputDetectListener(OnSoftInputDetectListener onSoftInputDetectListener) {
        this.onSoftInputDetectListener = onSoftInputDetectListener;
    }

    public boolean isSoftInputShowing() {
        return softInputShowingStatus == SoftInputShowingStatus.SHOW;
    }

    enum SoftInputShowingStatus {
        SHOW, HIDE
    }

    public interface OnSoftInputDetectListener {
        void onSoftInputDetected(boolean isShowing, int softInputHeight);
    }
}
