package com.tosslab.jandi.app.ui.message.v2.model;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;

import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;
import com.tosslab.jandi.app.views.listeners.SimpleListViewScrollListener;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Steve SeongUg Jung on 15. 2. 16..
 */
public class DisableLayoutUtil {

    public static void setDisableLayoutController(StickyListHeadersListView listview, View disableLayout) {

        final View _disableLayout = disableLayout;

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
                        show(_disableLayout);
                        lastScrollY = firstItemTop;
                        Log.d("INFO", "Show~!");
                    } else if (lastScrollY < firstItemTop) {
                        hide(_disableLayout);
                        lastScrollY = firstItemTop;
                        Log.d("INFO", "Hide~!");
                    }


                } else {

                    if (firstVisibleItem > lastItemPosition) {
                        show(_disableLayout);
                        lastScrollY = firstItemTop;
                        lastItemPosition = firstVisibleItem;
                        Log.d("INFO", "Show~!");
                    } else if (firstVisibleItem < lastItemPosition) {
                        hide(_disableLayout);
                        lastScrollY = firstItemTop;
                        lastItemPosition = firstVisibleItem;
                        Log.d("INFO", "Hide~!");
                    }
                }

            }

            private void hide(View fab) {

                if (isAnimating) {
                    return;
                }


                isAnimating = true;

                Animation translateAnimation = new ScaleAnimation(0f, 0f, 1f, 2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                translateAnimation.setInterpolator(fab.getContext(), android.R.anim.accelerate_interpolator);
                translateAnimation.setDuration(fab.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));
                translateAnimation.setFillAfter(true);
                translateAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis());

                fab.startAnimation(translateAnimation);

                translateAnimation.setAnimationListener(new SimpleEndAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        isAnimating = false;
                    }
                });

            }

            private void show(View fab) {

                if (isAnimating) {
                    return;
                }

                isAnimating = true;

                Animation translateAnimation = new ScaleAnimation(0f, 0f, 2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                translateAnimation.setInterpolator(fab.getContext(), android.R.anim.accelerate_interpolator);
                translateAnimation.setDuration(fab.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));
                translateAnimation.setFillAfter(true);
                translateAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis());

                translateAnimation.setAnimationListener(new SimpleEndAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        isAnimating = false;
                    }
                });

                fab.startAnimation(translateAnimation);

            }
        });


    }

}
