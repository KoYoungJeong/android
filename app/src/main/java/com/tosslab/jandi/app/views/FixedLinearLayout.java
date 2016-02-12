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
        int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int childCount = getChildCount();
        if (childCount >= 2) {
            View firstChild = getChildAt(0);
            LinearLayout.LayoutParams firstChildParams = (LayoutParams) firstChild.getLayoutParams();
            int leftSideWidth = firstChild.getMeasuredWidth()
                    + firstChildParams.leftMargin + firstChildParams.rightMargin;

            int rightSideWidth = 0;
            for (int i = 1; i < childCount; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == View.GONE) {
                    continue;
                }
                child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
                LinearLayout.LayoutParams childParams = (LayoutParams) child.getLayoutParams();
                rightSideWidth +=
                        child.getMeasuredWidth() + childParams.leftMargin + childParams.rightMargin;
            }

            if (width < (leftSideWidth + rightSideWidth)) {
                leftSideWidth = width - rightSideWidth;

                int firstChildWidth = leftSideWidth
                        - firstChildParams.leftMargin - firstChildParams.rightMargin;

                firstChildParams.width = firstChildWidth;
                firstChild.setLayoutParams(firstChildParams);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
