package com.tosslab.jandi.app.views.eastereggs;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 15. 11. 30..
 */
public class SnowView extends View {
    public static final String TAG = SnowView.class.getSimpleName();

    public static final int BACKGROUND_COLOR = Color.parseColor("#e1e9ec");

    private Drawable[] snows;

    private int previousOrientation;

    public SnowView(Context context) {
        super(context);
        init();
    }

    public SnowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SnowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setBackgroundColor(BACKGROUND_COLOR);

        previousOrientation = getContext().getResources().getConfiguration().orientation;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        if (snows != null) {
            return;
        }

        int displayHeight = getContext().getResources().getDisplayMetrics().heightPixels;

        int arrLength = displayHeight / 17;
        snows = new SnowAnimationDrawable[arrLength];
        Interpolator interpolator = new LinearInterpolator();
        for (int i = 0; i < snows.length; i++) {
            float fromX = (float) (Math.random() * width);
            float toX = fromX + getPixelFromDp(10);
            float fromY = i * getPixelFromDp(10);

            TranslateAnimation animation = new TranslateAnimation(fromX, toX, -fromY, displayHeight);
            animation.setInterpolator(interpolator);
            long duration = (long) (displayHeight + fromY) * 2;
            animation.setDuration(duration);
            animation.setRepeatCount(Animation.INFINITE);
            animation.initialize(0, 0, 0, 0);
            animation.startNow();

            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
            alphaAnimation.setDuration(duration);
            alphaAnimation.setRepeatCount(Animation.INFINITE);
            alphaAnimation.initialize(0, 0, 0, 0);
            alphaAnimation.startNow();

            AnimationSet animationSet = new AnimationSet(true);
            animationSet.addAnimation(alphaAnimation);
            animationSet.addAnimation(animation);

            Drawable drawable = getContext().getResources().getDrawable(R.drawable.snow_02);
            int intrinsicWidth = (int) (drawable.getIntrinsicWidth() * 1.5);
            int r = (int) (Math.random() * intrinsicWidth);
            drawable.setBounds(0, 0, r, r);

            snows[i] = new SnowAnimationDrawable(animationSet, drawable);;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (snows == null || snows.length <= 0) {
            return;
        }

        for (int i = 0; i < snows.length; i++) {
            Drawable snow = snows[i];
            canvas.save();
            snow.draw(canvas);
            canvas.restore();
        }

        invalidate();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (previousOrientation != newConfig.orientation) {
            if (snows != null) {
                snows = null;
            }
            previousOrientation = newConfig.orientation;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (snows != null) {
            snows = null;
        }
        super.onDetachedFromWindow();
    }

    private float getPixelFromDp(float px) {
        if (Math.abs(px) <= 0) {
            return 0;
        }
        return getContext().getResources().getDisplayMetrics().density * px;
    }

}
