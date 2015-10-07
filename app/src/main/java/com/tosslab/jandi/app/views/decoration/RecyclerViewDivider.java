package com.tosslab.jandi.app.views.decoration;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by tonyjs on 15. 10. 7..
 */
public class RecyclerViewDivider extends RecyclerView.ItemDecoration {
    public static final int VERTICAL = 0;
    public static final int HORIZONTAL = 1;

    public static final int DEFAULT_COLOR = Color.TRANSPARENT;
    public static final float DEFAULT_SIZE = 1f;

    private float mSize = DEFAULT_SIZE;
    private int mColor = DEFAULT_COLOR;

    private int mOrientation = VERTICAL;

    public RecyclerViewDivider() {
        this(DEFAULT_SIZE);
    }

    public RecyclerViewDivider(float size) {
        this(size, DEFAULT_COLOR);
    }

    public RecyclerViewDivider(float size, int color) {
        this(size, color, VERTICAL);
    }

    public RecyclerViewDivider(float size, int color, int orientation){
        mSize = size;
        mColor = color;
        mOrientation = orientation;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        Paint paint = new Paint();
        paint.setColor(mColor);
        paint.setStrokeWidth(mSize);

        switch (mOrientation) {
            case VERTICAL:
                drawVertical(c, parent, paint);
                break;
            case HORIZONTAL:
                drawHorizontal(c, parent, paint);
                break;
        }
    }

    void drawVertical(Canvas c, RecyclerView parent, Paint paint) {
        final float left = parent.getPaddingLeft();
        final float right = parent.getWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount() - 1;
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params =
                    (RecyclerView.LayoutParams) child.getLayoutParams();
            final float top = child.getBottom() + params.bottomMargin;
            final float startY = top + (mSize / 2);
            c.drawLine(left, startY, right, startY, paint);
        }
    }

    void drawHorizontal(Canvas c, RecyclerView parent, Paint paint) {
        final float top = parent.getPaddingTop();
        final float bottom = parent.getHeight() - parent.getPaddingBottom();
        final int childCount = parent.getChildCount() - 1;
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params =
                    (RecyclerView.LayoutParams) child.getLayoutParams();
            final float left = child.getRight() + params.rightMargin;
            final float startX = left + (mSize / 2);
            c.drawLine(startX, top, startX, bottom, paint);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        switch (mOrientation) {
            case VERTICAL:
                outRect.set(0, 0, 0, (int) Math.ceil(mSize));
                break;
            case HORIZONTAL:
                outRect.set(0, 0, (int) Math.ceil(mSize), 0);
                break;
        }
    }
}
