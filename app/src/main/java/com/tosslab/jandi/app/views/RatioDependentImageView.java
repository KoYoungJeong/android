package com.tosslab.jandi.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by tonyjs on 16. 5. 9..
 */
public class RatioDependentImageView extends ImageView {

    public RatioDependentImageView(Context context) {
        super(context);
    }

    public RatioDependentImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RatioDependentImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private float ratio = 0;

    public void setRatio(float ratio) {
        this.ratio = ratio;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        // WRAP_CONTENT
        if (mode == MeasureSpec.AT_MOST
                || ratio <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (width * ratio);

        setMeasuredDimension(width, height);
    }
}
