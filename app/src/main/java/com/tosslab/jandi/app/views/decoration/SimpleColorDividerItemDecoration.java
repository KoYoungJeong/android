package com.tosslab.jandi.app.views.decoration;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tosslab.jandi.app.utils.UiUtils;

/**
 * Created by Steve SeongUg Jung on 15. 5. 12..
 */
public class SimpleColorDividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDivider;
    private final int height;

    public SimpleColorDividerItemDecoration(int color) {
        mDivider = new ColorDrawable(color);
        height = (int) UiUtils.getPixelFromDp(1f);
    }
    public SimpleColorDividerItemDecoration(int color, int height) {
        mDivider = new ColorDrawable(color);
        this.height = height;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + height;

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}
