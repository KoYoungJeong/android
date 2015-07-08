package com.tosslab.jandi.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by Steve SeongUg Jung on 15. 7. 7..
 */
public class FixedLinearLayout extends LinearLayout {
    public FixedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() == 2) {
            View firstChild = getChildAt(0);
            View secondChild = getChildAt(1);

            int secondRighPosition = firstChild.getWidth()
                    + secondChild.getWidth()
                    + ((LayoutParams) secondChild.getLayoutParams()).leftMargin;

            if (getMeasuredWidth() <= secondRighPosition) {
                LayoutParams layoutParams = (LayoutParams) firstChild.getLayoutParams();

//                layoutParams.width = getMeasuredWidth() - secondChild.getMeasuredWidth()
//                        - ((LayoutParams) secondChild.getLayoutParams()).leftMargin;

//                firstChild.setLayoutParams(layoutParams);


                LogUtil.d(String.format("%s : %d", ((TextView) firstChild).getText(), firstChild.getWidth()));

            }
        }

    }
}
