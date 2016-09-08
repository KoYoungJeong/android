package com.tosslab.jandi.app.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tee on 16. 1. 25..
 */
public class FloatingActionMenu extends FrameLayout {

    private static final int ANIMATION_DURATION = 300;

    // MAX 카운트를 수정하기 위해서 ids.xml에 아이디를 추가하도록 한다.
    private static final int ITEM_MAX_COUNT = 5;

    RelativeLayout rootView;
    private int buttonCnt = 0;

    private ImageView btMenuIcon;

    private List<View> vgItems;
    private List<View> btItems;
    private List<View> tvItems;

    private boolean isOpened = false;

    private AnimatorSet openAnimatorSet = new AnimatorSet();
    private AnimatorSet closeAnimatorSet = new AnimatorSet();

    private Interpolator openInterpolator = new OvershootInterpolator();
    private Interpolator closeInterpolator = new AnticipateInterpolator();

    private GestureDetector gestureDetector =
            new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return isOpened;
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });

    public FloatingActionMenu(Context context) {
        super(context);
        init();
    }

    public FloatingActionMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        rootView = new RelativeLayout(getContext());
        rootView.setVisibility(View.GONE);
        rootView.setBackgroundResource(R.color.jandi_transparent_black_50p);
        rootView.setOnTouchListener((v, event) -> {
            if (FloatingActionMenu.this.isOpened()) {
                FloatingActionMenu.this.close();
                return true;
            }
            return false;
        });

        View decorView = ((Activity) getContext()).getWindow().getDecorView();
        ViewGroup window = (ViewGroup) decorView.getRootView();
        LayoutParams params = new LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rootView.setLayoutParams(params);
        window.addView(rootView);

        btMenuIcon = new ImageView(getContext());
        RelativeLayout.LayoutParams menuIconParams =
                new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        menuIconParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        menuIconParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        btMenuIcon.setClickable(true);
        btMenuIcon.setId(R.id.fab_menu_button);
        btMenuIcon.setImageResource(R.drawable.btn_chat_fab);
        this.rootView.addView(btMenuIcon, menuIconParams);

        vgItems = new ArrayList<>();
        btItems = new ArrayList<>();
        tvItems = new ArrayList<>();

        setOnMenuButtonClicked();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).layout(l, t, r, b);
        }
    }

    public void addItem(int imageResId, String title, OnButtonClickListener listener)
            throws IndexOutOfBoundsException {
        if (buttonCnt == ITEM_MAX_COUNT) {
            throw new IndexOutOfBoundsException(
                    "max 개수가 넘어감 max를 수정하려면 ids에 \n" +
                            "1) ids.xml 파일에 id 추가\n" +
                            "2) ITEM_MAX_COUNT 수정 \n" +
                            "3) getButtonId() 수정 \n");
        }

        LinearLayout vg = new LinearLayout(getContext());
        RelativeLayout.LayoutParams vgParams =
                new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        vgParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        vgParams.addRule(RelativeLayout.ABOVE, getButtonId());
        vg.setOrientation(LinearLayout.HORIZONTAL);
        rootView.addView(vg, vgParams);
        vg.setVisibility(INVISIBLE);
        vgItems.add(vg);

        TextView tv = new TextView(getContext());
        tv.setTextColor(getResources().getColor(R.color.white));
        tv.setBackgroundResource(R.drawable.text_bg_fab);
        tv.setText(title);
        tv.setPadding(dpToPx(14), dpToPx(7), dpToPx(14), dpToPx(11));
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tvParams.gravity = Gravity.CENTER_VERTICAL;
        vg.addView(tv, tvParams);
        tvItems.add(tv);

        ImageView bt = new ImageView(getContext());
        LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(dpToPx(52), dpToPx(52));
        ivParams.setMargins(dpToPx(0), dpToPx(2), dpToPx(23), dpToPx(2));
        bt.setImageResource(imageResId);
        bt.setClickable(true);
        bt.setOnClickListener(v -> listener.click());
        vg.addView(bt, ivParams);
        btItems.add(bt);

        buttonCnt++;
        vg.setId(getButtonId());
    }

    // 버튼이 4개 이상 필요한 경우 ids.xml에 아이디 추가후 아래 케이스를 추가하여 사용하도록 한다.
    private int getButtonId() {
        switch (buttonCnt) {
            case 0:
                return R.id.fab_menu_button;
            case 1:
                return R.id.fab_button_1;
            case 2:
                return R.id.fab_button_2;
            case 3:
                return R.id.fab_button_3;
            case 4:
                return R.id.fab_button_4;
            case 5:
                return R.id.fab_button_5;
        }
        return -1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private void setOnMenuButtonClicked() {
        setOnMenuAnimation();
        btMenuIcon.setOnClickListener(v -> {
            if (isOpened) {
                close();
            } else {
                open();
            }
        });
    }

    public void open() {
        showMenu();
        showMenuItems();
    }

    public void close() {
        dismissMenu();
        dismissMenuItems();
    }

    private void showMenu() {
        isOpened = true;
        if (closeAnimatorSet.isRunning()) {
            closeAnimatorSet.cancel();
        }
        openAnimatorSet.start();
    }

    private void dismissMenu() {
        isOpened = false;
        if (openAnimatorSet.isRunning()) {
            openAnimatorSet.cancel();
        }
        closeAnimatorSet.start();
    }

    // 135도 돌리는 애니메이션 set
    private void setOnMenuAnimation() {
        ObjectAnimator expandAnimator = ObjectAnimator.ofFloat(
                btMenuIcon,
                "rotation",
                0f,
                135f
        );

        ObjectAnimator collapseAnimator = ObjectAnimator.ofFloat(
                btMenuIcon,
                "rotation",
                135f,
                0f
        );

        openAnimatorSet.play(expandAnimator);
        closeAnimatorSet.play(collapseAnimator);

        openAnimatorSet.setInterpolator(openInterpolator);
        closeAnimatorSet.setInterpolator(closeInterpolator);

        openAnimatorSet.setDuration(ANIMATION_DURATION);
        closeAnimatorSet.setDuration(ANIMATION_DURATION);

        closeAnimatorSet.addListener(new SimpleEndAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(false);
            }
        });
    }

    private void showMenuItems() {
        int timer = 0;
        for (int i = 0; i < buttonCnt; i++) {
            final View btItem = btItems.get(i);
            final View tvItem = tvItems.get(i);
            final View vgItem = vgItems.get(i);
            postDelayed(() -> {
                Animation scaleDownAnimation1 = AnimationUtils.loadAnimation(getContext(),
                        R.anim.fab_scale_up);
                Animation scaleDownAnimation2 = AnimationUtils.loadAnimation(getContext(),
                        R.anim.fab_scale_up);
                tvItem.clearAnimation();
                btItem.clearAnimation();
                tvItem.startAnimation(scaleDownAnimation1);
                btItem.startAnimation(scaleDownAnimation2);
                vgItem.setVisibility(VISIBLE);
            }, timer);
            timer += 50;
        }
    }

    private void dismissMenuItems() {
        int timer = 0;
        for (int i = buttonCnt - 1; i >= 0; i--) {
            final View btItem = btItems.get(i);
            final View tvItem = tvItems.get(i);
            final View vgItem = vgItems.get(i);
            postDelayed(() -> {
                Animation scaleDownAnimation1 = AnimationUtils.loadAnimation(getContext(),
                        R.anim.fab_scale_down);
                scaleDownAnimation1.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        vgItem.setVisibility(INVISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                Animation scaleDownAnimation2 = AnimationUtils.loadAnimation(getContext(),
                        R.anim.fab_scale_down);
                tvItem.clearAnimation();
                btItem.clearAnimation();
                tvItem.startAnimation(scaleDownAnimation1);
                btItem.startAnimation(scaleDownAnimation2);
            }, timer);
            timer += 50;
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, getResources().getDisplayMetrics());
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void setVisibility(boolean show) {
        rootView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setupButtonLocation(View fabButton) {
        post(() -> {
            MarginLayoutParams layoutParams = (MarginLayoutParams) btMenuIcon.getLayoutParams();
            layoutParams.rightMargin = rootView.getMeasuredWidth() - fabButton.getRight();
            layoutParams.bottomMargin = rootView.getMeasuredHeight() - fabButton.getBottom()
                    - UiUtils.getStatusBarHeight();
            btMenuIcon.setLayoutParams(layoutParams);
        });
    }

    public interface OnButtonClickListener {
        void click();
    }

}