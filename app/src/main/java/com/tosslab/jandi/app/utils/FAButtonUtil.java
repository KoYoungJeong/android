package com.tosslab.jandi.app.utils;

import android.support.annotation.InterpolatorRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;

import com.tosslab.jandi.app.views.listeners.SimpleListViewScrollListener;

import java.lang.ref.WeakReference;

/**
 * Created by Steve SeongUg Jung on 15. 2. 9..
 */
public class FAButtonUtil {
    @InterpolatorRes
    private static final int ACCELERATE_INTERPOLATOR = android.R.anim.accelerate_interpolator;

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
                if (firstView == null) {
                    return;
                }
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

                if (fab.getVisibility() == View.INVISIBLE || isAnimating) {
                    return;
                }

                isAnimating = true;

                TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 2f);
                translateAnimation.setInterpolator(fab.getContext(), ACCELERATE_INTERPOLATOR);
                translateAnimation.setDuration(fab.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));
                translateAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis());

                fab.startAnimation(translateAnimation);

                translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        fab.setVisibility(View.INVISIBLE);
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
                translateAnimation.setInterpolator(fab.getContext(), ACCELERATE_INTERPOLATOR);
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

    public static void setFAButtonController(RecyclerView listview, View faButton) {
        WeakReference<View> wrFaButton = new WeakReference<>(faButton);

        listview.setOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean isAnimating = false;
            int lastScrollY = 0;
            boolean isHide;

            private void hide() {
                if (wrFaButton != null && wrFaButton.get() != null) {
                    final View view = wrFaButton.get();
                    if (view.getVisibility() == View.INVISIBLE || isAnimating) {
                        return;
                    }

                    isAnimating = true;

                    TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 2f);
                    translateAnimation.setInterpolator(view.getContext(), ACCELERATE_INTERPOLATOR);
                    translateAnimation.setDuration(view.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));
                    translateAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis());
                    view.startAnimation(translateAnimation);

                    translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if (wrFaButton.get() != null) {
                                View view = wrFaButton.get();
                                view.setVisibility(View.INVISIBLE);
                                isAnimating = false;
                                view.clearAnimation();
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }

            }

            private void show() {
                if (wrFaButton != null && wrFaButton.get() != null) {
                    final View view = wrFaButton.get();
                    if (view.getVisibility() == View.VISIBLE || isAnimating) {
                        return;
                    }

                    isAnimating = true;

                    view.setVisibility(View.VISIBLE);

                    TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 2f, Animation.RELATIVE_TO_SELF, 0f);
                    translateAnimation.setInterpolator(view.getContext(), ACCELERATE_INTERPOLATOR);
                    translateAnimation.setDuration(view.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));
                    translateAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis());

                    translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if (wrFaButton != null && wrFaButton.get() != null) {
                                final View view = wrFaButton.get();
                                isAnimating = false;
                                view.clearAnimation();
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    view.startAnimation(translateAnimation);
                }
            }

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
}
