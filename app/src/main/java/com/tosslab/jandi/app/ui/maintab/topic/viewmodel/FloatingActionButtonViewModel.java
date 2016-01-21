package com.tosslab.jandi.app.ui.maintab.topic.viewmodel;

import android.support.annotation.InterpolatorRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;

import com.tosslab.jandi.app.libraries.floatingactionmenu.FloatingActionMenu;

/**
 * Created by tee on 16. 1. 20..
 */
public class FloatingActionButtonViewModel {

    @InterpolatorRes
    private static final int ACCELERATE_INTERPOLATOR = android.R.anim.accelerate_interpolator;
    static boolean isAnimating = false;
    private static FloatingActionMenu floatingActionMenu;
    private static RecyclerView lvTopic;

    public static void setFloatingActionMenu(FloatingActionMenu floatingActionMenu) {
        FloatingActionButtonViewModel.floatingActionMenu = floatingActionMenu;
        floatingActionMenu.setClosedOnTouchOutside(true);
    }

    public static void setLvTopic(RecyclerView lvTopic) {
        FloatingActionButtonViewModel.lvTopic = lvTopic;
    }

    public static void setFAButtonController() {
        if (floatingActionMenu == null) {
            return;
        }

        lvTopic.setOnScrollListener(new RecyclerView.OnScrollListener() {

            int lastScrollY = 0;
            boolean isHide;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    if (!isHide) {
                        hide();
                    }
                    isHide = true;
                } else {
                    if (isHide) {
                        show();
                    }
                    isHide = false;
                }
                lastScrollY = dy;
            }

        });
    }

    private static void hide() {

        if (floatingActionMenu.getVisibility() == View.INVISIBLE || isAnimating) {
            return;
        }

        isAnimating = true;

        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 2f);
        translateAnimation.setInterpolator(floatingActionMenu.getContext(), ACCELERATE_INTERPOLATOR);
        translateAnimation.setDuration(floatingActionMenu.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));
        translateAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis());

        floatingActionMenu.startAnimation(translateAnimation);


        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setFABMenuVisibility(false);
                isAnimating = false;
                floatingActionMenu.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }


    private static void show() {

        if (floatingActionMenu.getVisibility() == View.VISIBLE || isAnimating) {
            return;
        }

        isAnimating = true;

        setFABMenuVisibility(true);

        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 2f, Animation.RELATIVE_TO_SELF, 0f);
        translateAnimation.setInterpolator(floatingActionMenu.getContext(), ACCELERATE_INTERPOLATOR);
        translateAnimation.setDuration(floatingActionMenu.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));
        translateAnimation.setFillAfter(true);
        translateAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis());

        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnimating = false;
                floatingActionMenu.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        floatingActionMenu.startAnimation(translateAnimation);
    }

    public static void releaseFAButtonController() {
        if (lvTopic != null) {
            lvTopic.setOnScrollListener(null);
        }
    }

    public static void setFABMenuVisibility(boolean visibility) {
        if (floatingActionMenu == null) {
            return;
        }
        if (visibility) {
            floatingActionMenu.setVisibility(View.VISIBLE);
        } else {
            floatingActionMenu.setVisibility(View.INVISIBLE);
            if (floatingActionMenu.isOpened()) {
                floatingActionMenu.hideMenu(false);
            }
        }
    }

}
