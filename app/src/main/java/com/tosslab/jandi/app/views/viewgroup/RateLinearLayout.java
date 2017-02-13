package com.tosslab.jandi.app.views.viewgroup;


import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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
            View view2 = getChildAt(1);

            int layoutWidth = getMeasuredWidth();
            int rightMargin = ((LayoutParams) view.getLayoutParams()).rightMargin;

            view2.measure(MeasureSpec.UNSPECIFIED, heightMeasureSpec);
            int view2Width = getViewWidth(view2);

            int maxWidth;
            if (view2Width > layoutWidth * 0.3) {
                maxWidth = (int) (layoutWidth * 0.7) - rightMargin;
            } else {
                maxWidth = layoutWidth - rightMargin - view2Width;
            }

            if (view.getMeasuredWidth() > maxWidth) {
                view.measure(MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
            }

            maxWidth = layoutWidth - view.getMeasuredWidth() - rightMargin;
            view2.measure(MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY), heightMeasureSpec);

        }
    }

    @Override
    protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
        super.measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    private int getViewWidth(View view2) {
        TextView tv = (TextView) view2;
        return tv.getVisibility() != View.GONE && tv.length() > 0 ? tv.getMeasuredWidth() : 0;
    }
}
