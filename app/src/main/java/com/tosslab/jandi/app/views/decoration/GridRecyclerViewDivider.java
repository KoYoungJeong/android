package com.tosslab.jandi.app.views.decoration;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by tonyjs on 15. 10. 13..
 */
public class GridRecyclerViewDivider extends RecyclerView.ItemDecoration {
    public static final int DEFAULT_COLOR = Color.TRANSPARENT;
    public static final float DEFAULT_SIZE = 1f;

    private float size = DEFAULT_SIZE;
    private int color = DEFAULT_COLOR;

    public GridRecyclerViewDivider() {
        this(DEFAULT_SIZE);
    }

    public GridRecyclerViewDivider(float size) {
        this(size, DEFAULT_COLOR);
    }

    public GridRecyclerViewDivider(float size, int color) {
        this.size = size;
        this.color = color;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (!(parent.getLayoutManager() instanceof GridLayoutManager)) {
            return;
        }
        GridLayoutManager layoutManager = ((GridLayoutManager) parent.getLayoutManager());
        int spanCount = layoutManager.getSpanCount();

        int childLayoutPosition = parent.getChildLayoutPosition(view);
        if (childLayoutPosition >= spanCount) {
            outRect.top = (int) size;
        } else {
            outRect.top = 0;
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (color == DEFAULT_COLOR) {
            return;
        }

        if (!(parent.getLayoutManager() instanceof GridLayoutManager)) {
            return;
        }
        GridLayoutManager layoutManager = ((GridLayoutManager) parent.getLayoutManager());
        int spanCount = layoutManager.getSpanCount();

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(size);

        final float left = parent.getPaddingLeft();
        final float right = parent.getWidth() - parent.getPaddingRight();

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int childLayoutPosition = parent.getChildLayoutPosition(child);
            if (childLayoutPosition >= spanCount) {
                final RecyclerView.LayoutParams params =
                        (RecyclerView.LayoutParams) child.getLayoutParams();
                final float top = child.getTop() + params.topMargin;
                final float startY = top - (size / 2);
                c.drawLine(left, startY, right, startY, paint);
            }
        }

    }

}
