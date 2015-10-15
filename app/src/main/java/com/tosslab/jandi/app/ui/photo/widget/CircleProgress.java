package com.tosslab.jandi.app.ui.photo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by tonyjs on 15. 6. 8..
 */
public class CircleProgress extends View {
    public static final String TAG = "CircleProgress";
    public static final int DEFAULT_BG_STROKE_WIDTH = 1;
    public static final int DEFAULT_PROGRESS_STROKE_WIDTH = 2;
    public static final int DEFAULT_BG_COLOR = Color.GRAY;
    public static final int DEFAULT_PROGRESS_COLOR = Color.BLUE;

    private Paint bgPaint;
    private Paint progressPaint;
    private RectF rectF;

    private int viewWidth;
    private int viewHeight;
    private int bgStrokeWidth;
    private int progressStrokeWidth;
    private int bgColor = DEFAULT_BG_COLOR;
    private int progressColor = DEFAULT_PROGRESS_COLOR;

    private int max = 100;
    private int progress = 0;

    public CircleProgress(Context context) {
        this(context, null);
    }

    public CircleProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);

        bgPaint = new Paint();
        progressPaint = new Paint();
        rectF = new RectF();

        float density = getContext().getResources().getDisplayMetrics().density;
        bgStrokeWidth = (int) (density * DEFAULT_BG_STROKE_WIDTH);
        progressStrokeWidth = (int) (density * DEFAULT_PROGRESS_STROKE_WIDTH);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.restore();

        int halfOfWidth = viewWidth / 2;
        int halfOfHeight = viewHeight / 2;

        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setColor(bgColor);
        bgPaint.setAntiAlias(true);
        bgPaint.setDither(true);
        bgPaint.setStrokeWidth(bgStrokeWidth);
        canvas.drawCircle(halfOfWidth, halfOfHeight, (halfOfWidth - (bgStrokeWidth / 2)), bgPaint);

        if (max <= 0) {
            canvas.save();
            return;
        }

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(progressColor);
        progressPaint.setAntiAlias(true);
        progressPaint.setDither(true);
        progressPaint.setStrokeWidth(progressStrokeWidth);

        rectF.left = progressStrokeWidth / 2;
        rectF.top = halfOfHeight - halfOfWidth + (progressStrokeWidth / 2);
        rectF.right = viewWidth - (progressStrokeWidth / 2);
        rectF.bottom = halfOfHeight + halfOfWidth - (progressStrokeWidth / 2);

        final float angle = (progress / (float) max) * 360;
        canvas.drawArc(rectF, -90, angle, false, progressPaint);

        canvas.save();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
    }

    public void setMax(int max) {
        if (max >= 0 && max != this.max) {
            this.max = max;
            invalidate();
        }
    }

    public void setProgress(int progress) {
        if (progress >= 0 && progress != this.progress) {
            this.progress = progress;
            invalidate();
        }
    }

    public void setBgStrokeWidth(int bgStrokeWidth) {
        if (this.bgStrokeWidth == bgStrokeWidth) {
            return;
        }
        this.bgStrokeWidth = bgStrokeWidth;
        invalidate();
    }

    public void setProgressStrokeWidth(int progressStrokeWidth) {
        if (this.progressStrokeWidth == progressStrokeWidth) {
            return;
        }
        this.progressStrokeWidth = progressStrokeWidth;
        invalidate();
    }

    public void setBgColor(int bgColor) {
        if (this.bgColor == bgColor) {
            return;
        }
        this.bgColor = bgColor;
        invalidate();
    }

    public void setProgressColor(int progressColor) {
        if (this.progressColor == progressColor) {
            return;
        }
        this.progressColor = progressColor;
        invalidate();
    }
}
