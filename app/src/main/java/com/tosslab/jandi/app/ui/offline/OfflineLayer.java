package com.tosslab.jandi.app.ui.offline;

import android.support.annotation.InterpolatorRes;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;

public class OfflineLayer {
    @InterpolatorRes
    private static final int DECELERATE_INTERPOLATOR = android.R.anim.decelerate_interpolator;

    private View vgOffline;

    public OfflineLayer(View vgOffline) {
        this.vgOffline = vgOffline;
    }

    public void dismissOfflineView() {

        if (vgOffline.getVisibility() != View.VISIBLE) {
            return;
        }

        if (vgOffline.getAnimation() != null && !vgOffline.getAnimation().hasEnded()) {
            vgOffline.getAnimation().cancel();
            vgOffline.clearAnimation();
        }

        Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -1f);

        animation.setDuration(JandiApplication.getContext().getResources().getInteger(android.R
                .integer
                .config_shortAnimTime));
        animation.setInterpolator(JandiApplication.getContext(), DECELERATE_INTERPOLATOR);
        animation.setStartTime(AnimationUtils.currentAnimationTimeMillis());
        animation.setFillAfter(true);

        animation.setAnimationListener(new SimpleEndAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                vgOffline.setVisibility(View.GONE);
            }
        });

        vgOffline.startAnimation(animation);
    }

    public void showOfflineView() {

        if (vgOffline.getVisibility() == View.VISIBLE) {
            return;
        }


        if (vgOffline.getAnimation() != null && !vgOffline.getAnimation().hasEnded()) {
            vgOffline.getAnimation().cancel();
            vgOffline.clearAnimation();
        }

        vgOffline.setVisibility(View.VISIBLE);

        Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -1f,
                Animation.RELATIVE_TO_SELF, 0f);

        animation.setDuration(JandiApplication.getContext().getResources().getInteger(android.R
                .integer.config_shortAnimTime));
        animation.setInterpolator(JandiApplication.getContext(), DECELERATE_INTERPOLATOR);
        animation.setStartTime(AnimationUtils.currentAnimationTimeMillis());
        animation.setFillAfter(true);

        vgOffline.startAnimation(animation);
    }
}
