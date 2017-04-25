package com.tosslab.jandi.app.views;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonyjs on 15. 9. 7..
 */
public class SwipeExitLayout extends FrameLayout {

    public static final int MIN_CANCEL_ANIM_DURATION = 200;
    public static final int MIN_EXIT_ANIM_DURATION = 500;
    public static final String TAG = SwipeExitLayout.class.getSimpleName();
    private static final float CANCELABLE_PIXEL_AMOUNT = 80;
    private static final float TOO_MANY_SCROLL_PIXEL_AMOUNT = 400;
    private static final float MIN_DIM_AMOUNT = 0.1f;
    private static final float MAX_DIM_AMOUNT = 0.9f;
    private static final int NEEDLESS_ALPHA_VALUE = -1;
    private GestureDetectorCompat gestureDetector;
    private float cancelablePixelAmount;
    private float tooManyScrollPixelAmount;
    private OnExitListener onExitListener;
    private StatusListener statusListener;
    private List<StatusListener> statusListenerList;
    private View vBackgroundDim;

    public SwipeExitLayout(Context context) {
        super(context);
        init(context);
    }

    public SwipeExitLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeExitLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        gestureDetector = new GestureDetectorCompat(getContext(), new GestureListener());
        cancelablePixelAmount = context.getResources().getDisplayMetrics().density * CANCELABLE_PIXEL_AMOUNT;
        tooManyScrollPixelAmount = context.getResources().getDisplayMetrics().density * TOO_MANY_SCROLL_PIXEL_AMOUNT;
    }

    public void setOnExitListener(OnExitListener onExitListener) {
        this.onExitListener = onExitListener;
    }

    public void setStatusListener(StatusListener statusListener) {
        this.statusListener = statusListener;
    }

    public void addOnScrollListener(StatusListener statusListener) {
        if (statusListenerList == null) {
            statusListenerList = new ArrayList<>();
        }

        statusListenerList.add(statusListener);
    }

    public void setViewToAlpha(View dimView) {
        vBackgroundDim = dimView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean hasCounsumeTouchEvent = gestureDetector.onTouchEvent(event);
        int action = event.getAction();
        if (action == MotionEvent.ACTION_CANCEL
                || action == MotionEvent.ACTION_UP) {
            exitOrCancel();
            return true;
        }
        return hasCounsumeTouchEvent;
    }

    private void handleScroll(float distance) {
        setTranslationY(this, -distance);

        setBackgroundDim(distance);

        exitIfTooManyScroll();
    }

    private void setTranslationY(ViewGroup viewGroup, float distance) {
        int childCount = viewGroup.getChildCount();
        if (childCount <= 0) {
            return;
        }

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            float translateDistance = child.getTranslationY() + distance;

            float resultOfDistance = Math.max(0, translateDistance);

            child.setTranslationY(resultOfDistance);
        }

        deliverScrollDistance(getChildAt(0).getTranslationY());
    }

    private void deliverScrollDistance(float translateY) {
        if (statusListener != null) {
            statusListener.onTranslateY(translateY);
        }

        if (statusListenerList != null && !statusListenerList.isEmpty()) {
            for (StatusListener statusListener : statusListenerList) {
                statusListener.onTranslateY(translateY);
            }
        }
    }

    private void deliverCancel(float spareDistance, int cancelAnimDuration) {
        if (statusListener != null) {
            statusListener.onCancel(spareDistance, cancelAnimDuration);
        }

        if (statusListenerList != null && !statusListenerList.isEmpty()) {
            for (StatusListener statusListener : statusListenerList) {
                statusListener.onCancel(spareDistance, cancelAnimDuration);
            }
        }
    }

    private void deliverExit(float spareDistance, int exitAnimDuration) {
        if (statusListener != null) {
            statusListener.onExit(spareDistance, exitAnimDuration);
        }

        if (statusListenerList != null && !statusListenerList.isEmpty()) {
            for (StatusListener statusListener : statusListenerList) {
                statusListener.onExit(spareDistance, exitAnimDuration);
            }
        }
    }

    private void exitOrCancel() {
        int childCount = getChildCount();
        if (childCount <= 0) {
            return;
        }

        View firstChild = getChildAt(0);
        float translationY = firstChild.getTranslationY();
        if (translationY > cancelablePixelAmount) {
            exit();
        } else {
            cancel(childCount, (int) translationY);
        }
    }

    private void exitIfTooManyScroll() {
        int childCount = getChildCount();
        if (childCount <= 0) {
            return;
        }

        View firstChild = getChildAt(0);
        float translationY = firstChild.getTranslationY();
        if (translationY >= tooManyScrollPixelAmount) {
            exit();
        }
    }

    private void exit() {
        int childCount = getChildCount();
        if (childCount <= 0) {
            return;
        }

        int measuredHeight = getMeasuredHeight();

        View firstChild = getChildAt(0);

        float distance = measuredHeight - firstChild.getTranslationY();
        int duration = Math.min(MIN_EXIT_ANIM_DURATION, (int) distance);
        if (duration <= 0) {
            return;
        }

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.animate()
                    .translationYBy(measuredHeight)
                    .setDuration(duration);
        }

        deliverExit(distance, duration);
    }

    private void cancel(int childCount, int translationY) {
        int duration = Math.min(MIN_CANCEL_ANIM_DURATION, translationY);

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            child.animate()
                    .setDuration(Math.abs(duration))
                    .translationY(0);
        }

        setBackgroundDimWithDuration(NEEDLESS_ALPHA_VALUE, 0.9f, duration);

        deliverCancel(0, duration);
    }

    private void setBackgroundDim(float distance) {
        if (vBackgroundDim == null) {
            return;
        }

        float ratio = distance / getMeasuredHeight();
        float dimAmount = vBackgroundDim.getAlpha();
        float resultDimAmount = dimAmount + ratio;
        if (resultDimAmount < MIN_DIM_AMOUNT) {
            resultDimAmount = MIN_DIM_AMOUNT;
        } else if (resultDimAmount > MAX_DIM_AMOUNT) {
            resultDimAmount = MAX_DIM_AMOUNT;
        }
        vBackgroundDim.setAlpha(resultDimAmount);
    }

    private void setBackgroundDimWithDuration(float fromAlpha, float toAlpha, int duration) {
        if (vBackgroundDim == null) {
            return;
        }

        if (fromAlpha != NEEDLESS_ALPHA_VALUE) {
            vBackgroundDim.setAlpha(fromAlpha);
        }

        vBackgroundDim.animate()
                .alpha(toAlpha)
                .setDuration(duration);
    }

    public interface OnExitListener {
        void onExit();
    }

    public interface StatusListener {
        void onTranslateY(float translateY);

        void onCancel(float spareDistance, int cancelAnimDuration);

        void onExit(float spareDistance, int exitAnimDuration);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(distanceY) > Math.abs(distanceX)) {
                handleScroll(distanceY);
            }
            return true;
        }
    }

}
