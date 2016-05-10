package com.tosslab.jandi.app.ui.message.v2.viewmodel;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

public class DateAnimator {
    private static final String TAG = "DateAnimator";
    private static final long DURATION = 1500;
    private final ValueAnimator valueAnimator;
    private View target;

    public DateAnimator(View target) {
        this.target = target;
        target.setAlpha(0f);
        valueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(DURATION);
        valueAnimator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            target.setAlpha(animatedValue);
        });

        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    public void show() {
        Log.d(TAG, "show() called with: " + "");

        long currentPlayTime = valueAnimator.getCurrentPlayTime();
        valueAnimator.removeAllListeners();
        valueAnimator.cancel();
        valueAnimator.setFloatValues(target.getAlpha(), 1f);
        valueAnimator.setDuration(currentPlayTime <= 0 ? DURATION : currentPlayTime);
        valueAnimator.start();
        target.setVisibility(View.VISIBLE);
    }

    public void hide() {
        Log.d(TAG, "hide() called with: ");
        long currentPlayTime = valueAnimator.getCurrentPlayTime();
        valueAnimator.removeAllListeners();
        valueAnimator.cancel();
        valueAnimator.setFloatValues(target.getAlpha(), 0f);
        valueAnimator.setDuration(currentPlayTime <= 0 ? DURATION : currentPlayTime);
        valueAnimator.addListener(new SimpleEndAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                target.setVisibility(View.GONE);
            }
        });
        valueAnimator.start();
    }
}
