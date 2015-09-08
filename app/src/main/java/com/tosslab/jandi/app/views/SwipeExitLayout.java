package com.tosslab.jandi.app.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonyjs on 15. 9. 7..
 */
public class SwipeExitLayout extends FrameLayout {
    public interface OnExitListener {
        void onExit();
    }

    public interface OnScrollListener {
        void onScroll(float distance);
    }

    public static final String TAG = SwipeExitLayout.class.getSimpleName();
    private static final float IGNORABLE_PIXEL_AMOUNT = 200;
    private static final float MIN_DIM_AMOUNT = 0.1f;
    private static final float MAX_DIM_AMOUNT = 0.9f;
    private static final int NEEDLESS_ALPHA_VALUE = -1;

    // For touch event.
    private float lastInterceptX;
    private float lastInterceptY;
    private SwipeGestureDetector gestureDetector;

    private float ignorablePixelAmount;

    private OnExitListener onExitListener;
    private OnScrollListener onScrollListener;
    private List<OnScrollListener> onScrollListenerList;

    private View vBackgroundDim;

    private AnimatorListenerAdapter exitListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            if (onExitListener != null) {
                onExitListener.onExit();
            }
        }
    };

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
        gestureDetector = new SwipeGestureDetector(getContext(), new GestureListener());
        ignorablePixelAmount = context.getResources().getDisplayMetrics().density * IGNORABLE_PIXEL_AMOUNT;
    }

    public void setOnExitListener(OnExitListener onExitListener) {
        this.onExitListener = onExitListener;
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    public void addOnScrollListener(OnScrollListener onScrollListener) {
        if (onScrollListenerList == null) {
            onScrollListenerList = new ArrayList<>();
        }

        onScrollListenerList.add(onScrollListener);
    }

    public void sevBackgroundDimView(View dimView) {
        vBackgroundDim = dimView;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (getChildCount() <= 0) {
            return true;
        }

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastInterceptX = ev.getX();
                lastInterceptY = ev.getY();
                return false;
            case MotionEvent.ACTION_MOVE:
                float x = ev.getX();
                float y = ev.getY();

                float absDistanceY = Math.abs(y - lastInterceptY);
                if (absDistanceY < ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                    return false;
                }

                float absDistanceX = Math.abs(x - lastInterceptX);
                if (absDistanceY > absDistanceX) {
                    return true;
                }
                return false;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                lastInterceptX = 0;
                lastInterceptY = 0;
            default:
                return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private void handleScroll(float distance) {
        setTranslationY(this, -distance);

        setBackgroundDim(distance);

        deliverScrollDistance(distance);
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
    }

    private void deliverScrollDistance(float distance) {
        if (onScrollListener != null) {
            onScrollListener.onScroll(distance);
        }

        if (onScrollListenerList != null && !onScrollListenerList.isEmpty()) {
            for (OnScrollListener onScrollListener : onScrollListenerList) {
                onScrollListener.onScroll(distance);
            }
        }
    }

    private void exitOrIgnore() {
        int childCount = getChildCount();
        if (childCount <= 0) {
            return;
        }

        View firstChild = getChildAt(0);
        float translationY = firstChild.getTranslationY();
        int distance;
        int duration;
        Animator.AnimatorListener listener = null;
        float alpha = 0.9f;

        if (translationY > ignorablePixelAmount) {
            alpha = 0.1f;
            distance = getMeasuredHeight();
            duration = Math.min(250, getMeasuredHeight() - (int) translationY);
            listener = exitListener;
        } else {
            distance = 0;
            duration = Math.min(300, (int) translationY);
        }

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            child.animate()
                    .setDuration(Math.abs(duration))
                    .translationY(distance)
                    .setListener(listener);
        }

        setBackgroundDimWithAlpha(NEEDLESS_ALPHA_VALUE, alpha, duration);
    }

    public void exit() {
        int childCount = getChildCount();
        if (childCount <= 0) {
            return;
        }

        View firstChild = getChildAt(0);
        float translationY = firstChild.getTranslationY();
        int distance = getMeasuredHeight();
        int duration = Math.min(250, getMeasuredHeight() - (int) translationY);

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.animate()
                    .setDuration(Math.abs(duration))
                    .translationY(distance)
                    .setListener(exitListener);
        }

        setBackgroundDimWithAlpha(0.9f, 0.1f, duration);
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

    private void setBackgroundDimWithAlpha(float fromAlpha, float toAlpha, int duration) {
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

    private class SwipeGestureDetector extends GestureDetectorCompat {
        GestureListener gestureListener;

        public SwipeGestureDetector(Context context, GestureListener listener) {
            super(context, listener);
            gestureListener = listener;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            boolean handleTouchEvent = super.onTouchEvent(event);
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP) {
                gestureListener.onSingleTapUp(event);
            }
            return handleTouchEvent;
        }
    }

    private class GestureListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            exitOrIgnore();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(distanceY) > Math.abs(distanceX)) {
                handleScroll(distanceY);
                return true;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return Math.abs(velocityY) > Math.abs(velocityX);
        }
    }

}
