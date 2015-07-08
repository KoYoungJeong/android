package com.tosslab.jandi.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by Steve SeongUg Jung on 15. 7. 7..
 */
public class FixedLinearLayout extends LinearLayout {
    public FixedLinearLayout(Context context) {
        this(context, null);
    }

    public FixedLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FixedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() == 2) {
            View firstChild = getChildAt(0);
            View secondChild = getChildAt(1);

            secondChild.measure(MeasureSpec.UNSPECIFIED, heightMeasureSpec);

            int secondRighPosition = firstChild.getMeasuredWidth()
                    + secondChild.getMeasuredWidth()
                    + ((LayoutParams) secondChild.getLayoutParams()).leftMargin;

            if (getMeasuredWidth() <= secondRighPosition) {


                int firstChildWidth = getMeasuredWidth() - secondChild.getMeasuredWidth()
                        - ((LayoutParams) secondChild.getLayoutParams()).leftMargin;

                firstChild.measure(
                        MeasureSpec.makeMeasureSpec(firstChildWidth, MeasureSpec.EXACTLY),
                        heightMeasureSpec);

            }
        }

    }
}
