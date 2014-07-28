package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Created by justinygchoi on 2014. 7. 12..
 * Selector 를 사용할 수 있는 FrameLayout
 * CDP List 선택시 FrameLayout이 state_selected 속성을 가질 수 있도록 상속 구현.
 */
public class SelectedLinearLayout extends LinearLayout {
    private static final int[] STATE_SELECTED = { android.R.attr.state_selected };

    private boolean mIsSelected = false;

    public SelectedLinearLayout(Context context) {
        super(context);
    }

    public SelectedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);

        if (mIsSelected)
            mergeDrawableStates(drawableState, STATE_SELECTED);

        return drawableState;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);

        if (mIsSelected != selected) {
            mIsSelected = selected;
            invalidate();
            refreshDrawableState();
        }
    }

}
