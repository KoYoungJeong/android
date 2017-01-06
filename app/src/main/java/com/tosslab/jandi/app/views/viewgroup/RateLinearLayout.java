package com.tosslab.jandi.app.views.viewgroup;


import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class RateLinearLayout extends LinearLayout {
    public RateLinearLayout(Context context) {
        this(context, null);
    }

    public RateLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RateLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() >= 2) {
            View view = getChildAt(0);

            int layoutWidth = getMeasuredWidth();
            int rightMargin = ((LayoutParams) view.getLayoutParams()).rightMargin;
            double maxWidth = layoutWidth * 0.7 - rightMargin;

            if (view.getMeasuredWidth() > maxWidth) {
                view.measure(MeasureSpec.makeMeasureSpec((int) (maxWidth), MeasureSpec.EXACTLY), heightMeasureSpec);
            }
            maxWidth = layoutWidth - view.getMeasuredWidth() - rightMargin;
            view = getChildAt(1);
            view.measure(MeasureSpec.makeMeasureSpec((int) (maxWidth), MeasureSpec.EXACTLY), heightMeasureSpec);
        }
    }
}
