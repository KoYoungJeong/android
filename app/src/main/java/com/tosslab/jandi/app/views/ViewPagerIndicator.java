package com.tosslab.jandi.app.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.tosslab.jandi.app.R;

/**
 * Created by Steve SeongUg Jung on 15. 6. 10..
 */
public class ViewPagerIndicator extends View {

    private int indicatorCount = 3;
    private int indicatorMargin;
    private int indicatorWidth;
    private int selectorColor;
    private int unselectorColor;
    private int currentPosition = 0;
    private int viewWidth;
    private int viewHeight;
    private int centerY;
    private Paint paint;

    public ViewPagerIndicator(Context context) {
        this(context, null);
    }

    public ViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);

        initAttrs(context, attrs);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    public void setCurrentPosition(int currentPosition) {
        if (this.currentPosition != currentPosition) {
            this.currentPosition = currentPosition;
            invalidate();
        }
    }

    public void setIndicatorCount(int indicatorCount) {
        if (this.indicatorCount != indicatorCount) {
            this.indicatorCount = indicatorCount;
            invalidate();
        }
    }

    private void initAttrs(Context context, AttributeSet attrs) {

        selectorColor = context.getResources().getColor(R.color.jandi_view_pager_indicator_selector);
        unselectorColor = context.getResources().getColor(R.color.jandi_view_pager_indicator_unselector);

        indicatorMargin = context.getResources().getDimensionPixelSize(R.dimen.jandi_view_pager_indicator_margin);
        indicatorWidth = context.getResources().getDimensionPixelSize(R.dimen.jandi_view_pager_indicator_size);

        currentPosition = indicatorCount / 2;

        if (attrs == null) {
            return;
        }

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator);

        selectorColor = a.getColor(R.styleable.ViewPagerIndicator_selectorColor, selectorColor);
        unselectorColor = a.getColor(R.styleable.ViewPagerIndicator_unselectorColor, unselectorColor);

        indicatorCount = a.getInt(R.styleable.ViewPagerIndicator_indicatorCount, indicatorCount);

        indicatorMargin = a.getDimensionPixelSize(R.styleable.ViewPagerIndicator_indicatorMargin, indicatorMargin);
        indicatorWidth = a.getDimensionPixelSize(R.styleable.ViewPagerIndicator_indicatorWidth, indicatorMargin);

        a.recycle();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (viewWidth < 0 || viewHeight < 0) {
            return;
        }

        canvas.restore();

        int drawWidth = (indicatorMargin + indicatorWidth) * indicatorCount - indicatorMargin;

        int firstDrawX = (viewWidth - drawWidth) / 2;

        for (int idx = 0; idx < indicatorCount; ++idx) {

            if (idx != currentPosition) {
                paint.setColor(unselectorColor);
            } else {
                paint.setColor(selectorColor);
            }

            canvas.drawCircle(firstDrawX + (indicatorWidth + indicatorMargin + indicatorWidth / 2) * idx + indicatorWidth / 2, centerY, indicatorWidth / 2, paint);
        }


        canvas.save();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        centerY = viewHeight / 2;
    }
}
