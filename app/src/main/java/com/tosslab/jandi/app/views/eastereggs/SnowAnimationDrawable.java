package com.tosslab.jandi.app.views.eastereggs;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;

/**
 * Created by tonyjs on 15. 12. 1..
 */
public class SnowAnimationDrawable extends Drawable {

    private Animation animation;
    private Drawable drawable;
    private Transformation transformation;

    public SnowAnimationDrawable(Animation animation, Drawable drawable) {
        this.animation = animation;
        this.drawable = drawable;
    }

    @Override
    public void draw(Canvas canvas) {
        if (drawable == null || animation == null) {
            return;
        }

        int save = canvas.save();
        transformation = new Transformation();
        animation.getTransformation(AnimationUtils.currentAnimationTimeMillis(), transformation);
        canvas.concat(transformation.getMatrix());
        drawable.draw(canvas);
        canvas.restoreToCount(save);
    }

    @Override
    public int getIntrinsicWidth() {
        return drawable.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return drawable.getIntrinsicHeight();
    }

    @Override
    public void setAlpha(int alpha) {
        drawable.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        drawable.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return drawable.getOpacity();
    }
}
