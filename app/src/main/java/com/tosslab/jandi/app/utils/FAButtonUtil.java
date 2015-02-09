package com.tosslab.jandi.app.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;

import com.tosslab.jandi.app.views.listeners.SimpleListViewScrollListener;

/**
 * Created by Steve SeongUg Jung on 15. 2. 9..
 */
public class FAButtonUtil {

    public static void setFAButtonController(AbsListView listview, View faButton) {

        final View fab = faButton;

        listview.setOnScrollListener(new SimpleListViewScrollListener() {

            int lastScrollY = 0;
            int lastItemPosition = 0;

            boolean isAnimating = false;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (totalItemCount <= 0) {
                    return;
                }

                View firstView = view.getChildAt(0);
                int firstItemTop = firstView.getTop();

                if (lastItemPosition == firstVisibleItem) {

                    if (Math.abs(lastScrollY - firstItemTop) <= 50) {
                        return;
                    }

                    if (lastScrollY > firstItemTop) {
                        hide(fab);
                        lastScrollY = firstItemTop;
                    } else if (lastScrollY < firstItemTop) {
                        show(fab);
                        lastScrollY = firstItemTop;
                    }


                } else {

                    if (firstVisibleItem > lastItemPosition) {
                        hide(fab);
                        lastScrollY = firstItemTop;
                        lastItemPosition = firstVisibleItem;
                    } else if (firstVisibleItem < lastItemPosition) {
                        show(fab);
                        lastScrollY = firstItemTop;
                        lastItemPosition = firstVisibleItem;
                    }
                }

            }

            private void hide(View fab) {

                if (fab.getVisibility() == View.GONE || isAnimating) {
                    return;
                }


                isAnimating = true;

                TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 2f);
                translateAnimation.setInterpolator(fab.getContext(), android.R.anim.accelerate_interpolator);
                translateAnimation.setDuration(fab.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));
                translateAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis());

                fab.startAnimation(translateAnimation);


                translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        fab.setVisibility(View.GONE);
                        isAnimating = false;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

            }

            private void show(View fab) {

                if (fab.getVisibility() == View.VISIBLE || isAnimating) {
                    return;
                }

                isAnimating = true;

                fab.setVisibility(View.VISIBLE);

                TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 2f, Animation.RELATIVE_TO_SELF, 0f);
                translateAnimation.setInterpolator(fab.getContext(), android.R.anim.accelerate_interpolator);
                translateAnimation.setDuration(fab.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));
                translateAnimation.setFillAfter(true);
                translateAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis());

                translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        isAnimating = false;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                fab.startAnimation(translateAnimation);

            }
        });


    }

}
