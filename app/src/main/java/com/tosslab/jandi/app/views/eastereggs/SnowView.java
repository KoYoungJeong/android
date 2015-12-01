package com.tosslab.jandi.app.views.eastereggs;

import android.content.Context;
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

/**
 * Created by tonyjs on 15. 11. 30..
 */
public class SnowView extends View {

//    private Snow[] snows;

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
        setBackgroundColor(Color.parseColor("#eaeaea"));
    }

    private boolean first = true;
    private Drawable[] snows;
    private int[][] uhm;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (first) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);

//            int size = height / 2;
            int size = height / 20;
            snows = new SnowAnimationDrawable[size];
            uhm = new int[size][];
//            float toX = width;
            Interpolator interpolator = new LinearInterpolator();
            for (int i = 0; i < snows.length; i++) {
                float fromX = (float) (Math.random() * width);
//                float toX = fromX + (width / 4);
                float toX = fromX + getDp(10);
                float fromY = i * getDp(10);

                TranslateAnimation animation = new TranslateAnimation(fromX, toX, -fromY, height);
                animation.setInterpolator(interpolator);
                long duration = (long) (height + fromY) * 2;
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
                int r = (int) (Math.random() * drawable.getIntrinsicWidth());
                drawable.setBounds(0, 0, r, r);

//                ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
//                int r = (int) (Math.random() * getDp(10));
//                shapeDrawable.setBounds(0, 0, r, r);
//                shapeDrawable.getPaint().setColor(Color.WHITE);
//                shapeDrawable.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

                uhm[i] = new int[]{(int) (Math.random() * width), -30};

                SnowAnimationDrawable snowAnimationDrawable = new SnowAnimationDrawable(animationSet, drawable);
                snows[i] = snowAnimationDrawable;
            }

//            snows = new Snow[height * 2];
//            for (int i = 0; i < snows.length; i++) {
//                float r = (float) (Math.random() * 10);
//                float x = (float) (Math.random() * width);
//                float y = -i * getDp(5);
//                Snow snow = new Snow(Color.WHITE, r, x, y);
//                snows[i] = snow;
//            }
            first = false;
        }
    }

    private int currentPosition = 0;

//    private void fall() {
//        for (int i = 0; i < snows.length; i++) {
//            Snow snow = snows[i];
//            float y = snow.getY() + getDp(5);
//            float x = y > 0 ? snow.getX() + getDp(getNextStepX()) : snow.getX();
//            if (y >= getMeasuredHeight()) {
//                y = -i;
//                x = (float) (Math.random() * getMeasuredWidth());
//            }
//            snow.setX(x);
//            snow.setY(y);
//        }
////        if (currentPosition >= snows.length) {
////            for (int i = 0; i < snows.length; i++) {
////                Snow snow = snows[i];
////                float x = snow.getX() + getDp(getNextStepX());
////                float y = snow.getY();
////                if (y >= getMeasuredHeight()) {
////                    y = 0;
////                    x = (float) (Math.random() * getMeasuredWidth());
////                }
////                snow.setX(x);
////                snow.setY(y + getDp(1));
////            }
////        } else {
////            for (int i = 0; i < currentPosition; i++) {
////                Snow snow = snows[i];
////                snow.setX(snow.getX() + getDp(getNextStepX()));
////                snow.setY(snow.getY() + getDp(1));
////            }
////            currentPosition += 1;
////        }
//
//        invalidate();
//
//        postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                fall();
//            }
//        }, 100);
//    }

    private float getNextStepX() {
        double rand = Math.random() * 3;
        if (rand > 2) {
            return +1;
        } else if (rand > 1) {
            return 0;
//            return -1;
        } else {
            return 0;
//            return -1;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                fall();
//            }
//        }, 100);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < snows.length; i++) {
            Drawable snow = snows[i];
            canvas.save();
//            canvas.translate(uhm[i][0], uhm[i][1]);
            snow.draw(canvas);
            canvas.restore();
        }
        invalidate();
//        Paint paint = new Paint();
//        paint.setColor(Color.WHITE);
//        paint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
//        for (int i = 0; i < snows.length; i++) {
//            Snow snow = snows[i];
//            Log.e("tony", String.format("%f, %f, %f", snow.getX(), snow.getY(), snow.getR()));
//            if (snow.getY() <= 0 || snow.getR() <= 0) {
//                continue;
//            }
//            canvas.drawCircle(snow.getX(), snow.getY(), snow.getR(), paint);
//        }
    }

    private float getDp(float px) {
        if (Math.abs(px) <= 0) {
            return 0;
        }
        return getContext().getResources().getDisplayMetrics().density * px;
    }

    private static class Snow {
        private int color;
        private float r;
        private float x;
        private float y;

        public Snow(int color, float r, float x, float y) {
            this.color = color;
            this.r = r;
            this.x = x;
            this.y = y;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public float getR() {
            return r;
        }

        public void setR(float r) {
            this.r = r;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }
    }


}
