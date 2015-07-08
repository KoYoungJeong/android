package com.tosslab.jandi.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by tonyjs on 15. 06. 16..
 */
public class AutoScaleImageView extends ImageView {

    public static final int NONE = -1;
    private float ratio = NONE;

    public AutoScaleImageView(Context context) {
        super(context);
    }

    public AutoScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoScaleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setRatio(int width, int height) {
        ratio = height / (float) width;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (getDrawable() != null) {
            int drawableWidth = getDrawable().getIntrinsicWidth();
            int drawableHeight = getDrawable().getIntrinsicHeight();

            int height = (int) (width * (drawableHeight / (float) drawableWidth));
            setMeasuredDimension(width, height);
        } else {
            if (ratio != NONE) {
                int height = (int) (width * ratio);
                setMeasuredDimension(width, height);
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }
    }
}