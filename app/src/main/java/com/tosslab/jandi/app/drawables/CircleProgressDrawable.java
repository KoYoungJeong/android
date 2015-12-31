package com.tosslab.jandi.app.drawables;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 15. 12. 16..
 */
public class CircleProgressDrawable extends Drawable {

    public interface OnFinishListener {
        void onFinish();
    }

    public static final String TAG = CircleProgressDrawable.class.getSimpleName();
    public static final int DEFAULT_PROGRESS_WIDTH_DP = 48;

    public static final int DEFAULT_TEXT_SIZE = 10;
    public static final int DEFAULT_TEXT_COLOR = Color.WHITE;

    public static final int DEFAULT_INDICATOR_TEXT_SIZE = 10;
    public static final int DEFAULT_INDICATOR_TEXT_MARGIN_DP = 10;
    public static final int DEFAULT_INDICATOR_TEXT_COLOR = 10;
    public static final String DEFAULT_INDICATOR_TEXT = "Downloading...";

    public static final int DEFAULT_BG_COLOR = Color.TRANSPARENT;
    public static final int DEFAULT_BG_STROKE_WIDTH = 1;
    public static final int DEFAULT_PROGRESS_STROKE_WIDTH = 2;
    public static final int DEFAULT_BG_PROGRESS_COLOR = Color.GRAY;
    public static final int DEFAULT_PROGRESS_COLOR = Color.BLUE;
    public static final float MAX_PROGRESS = 10000;

    private Paint bgPaint;
    private Paint bgProgressPaint;
    private Paint progressPaint;

    private Paint textPaint;
    private Paint indicatorTextPaint;

    private RectF rectF;

    private int progressWidth;

    private int textColor;
    private int textSize;

    private int indicatorTextSize;
    private int indicatorTextColor;
    private int indicatorTextMargin;
    private String indicatorText;

    private int backgroundColor = DEFAULT_BG_COLOR;
    private int bgStrokeWidth;
    private int progressStrokeWidth;
    private int bgProgressColor = DEFAULT_BG_PROGRESS_COLOR;
    private int progressColor = DEFAULT_PROGRESS_COLOR;

    private int topMargin;

    private int progress = 0;

    private OnFinishListener onFinishListener;

    public CircleProgressDrawable(Context context) {
        bgPaint = new Paint();
        bgProgressPaint = new Paint();
        progressPaint = new Paint();

        textPaint = new Paint();
        indicatorTextPaint = new Paint();

        rectF = new RectF();

        float density = context.getResources().getDisplayMetrics().density;
        progressWidth = (int) (density * DEFAULT_PROGRESS_WIDTH_DP);

        bgStrokeWidth = (int) (density * DEFAULT_BG_STROKE_WIDTH);
        progressStrokeWidth = (int) (density * DEFAULT_PROGRESS_STROKE_WIDTH);

        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        textColor = DEFAULT_TEXT_COLOR;
        textSize = (int) (scaledDensity * DEFAULT_TEXT_SIZE);

        indicatorTextColor = DEFAULT_INDICATOR_TEXT_COLOR;
        indicatorTextSize = (int) (scaledDensity * DEFAULT_INDICATOR_TEXT_SIZE);
        indicatorTextMargin = (int) (density * DEFAULT_INDICATOR_TEXT_MARGIN_DP);
        indicatorText = DEFAULT_INDICATOR_TEXT;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();

        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(backgroundColor);
        canvas.drawRect(0, 0, canvasWidth, canvasHeight, bgPaint);

        if (progressWidth >= canvasWidth) {
            progressWidth = canvasWidth;
        }

        int halfOfWidth = progressWidth / 2;

        int centerX = canvasWidth / 2;
        int centerY = topMargin > 0 ? topMargin + halfOfWidth : canvasHeight / 2;

        bgProgressPaint.setStyle(Paint.Style.STROKE);
        bgProgressPaint.setColor(bgProgressColor);
        bgProgressPaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        bgProgressPaint.setStrokeWidth(bgStrokeWidth);

        int radius = halfOfWidth;
        canvas.drawCircle(centerX, centerY, radius, bgProgressPaint);

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(progressColor);
        progressPaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        progressPaint.setStrokeWidth(progressStrokeWidth);

        int left = (centerX - halfOfWidth);
        rectF.left = left;
        int top = (centerY - halfOfWidth);
        rectF.top = top;
        rectF.right = left + progressWidth;
        rectF.bottom = top + progressWidth;

        float percentage = progress / MAX_PROGRESS;
        final float angle = percentage * 360;
        canvas.drawArc(rectF, -90, angle, false, progressPaint);

        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        String progressText = (int) (percentage * 100) + "%";

        Rect bounds = new Rect();
        textPaint.getTextBounds(progressText, 0, progressText.length(), bounds);

        int startX = centerX - (bounds.width() / 2);
        int endY = centerY + (bounds.height() / 2);

        canvas.drawText(progressText, startX, endY, textPaint);

        indicatorTextPaint.setColor(indicatorTextColor);
        indicatorTextPaint.setTextSize(indicatorTextSize);
        indicatorTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        indicatorTextPaint.getTextBounds(indicatorText, 0, indicatorText.length(), bounds);

        startX = centerX - (bounds.width() / 2);
        endY = centerY + radius + indicatorTextMargin + (bounds.height() / 2);

        canvas.drawText(indicatorText, startX, endY, indicatorTextPaint);

        canvas.restore();
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        if (progress >= 0 && progress != this.progress) {
            this.progress = progress;
            invalidateSelf();
        }
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setBgStrokeWidth(int bgStrokeWidth) {
        if (this.bgStrokeWidth == bgStrokeWidth) {
            return;
        }
        this.bgStrokeWidth = bgStrokeWidth;
        invalidateSelf();
    }

    public void setProgressStrokeWidth(int progressStrokeWidth) {
        if (this.progressStrokeWidth == progressStrokeWidth) {
            return;
        }
        this.progressStrokeWidth = progressStrokeWidth;
        invalidateSelf();
    }

    public void setBgProgressColor(int bgProgressColor) {
        if (this.bgProgressColor == bgProgressColor) {
            return;
        }
        this.bgProgressColor = bgProgressColor;
        invalidateSelf();
    }

    public void setProgressColor(int progressColor) {
        if (this.progressColor == progressColor) {
            return;
        }
        this.progressColor = progressColor;
        invalidateSelf();
    }

    public void setTopMargin(int topMargin) {
        this.topMargin = topMargin;
    }

    public int getProgressWidth() {
        return progressWidth;
    }

    public void setProgressWidth(int progressWidth) {
        this.progressWidth = progressWidth;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getIndicatorTextSize() {
        return indicatorTextSize;
    }

    public void setIndicatorTextSize(int indicatorTextSize) {
        this.indicatorTextSize = indicatorTextSize;
    }

    public int getIndicatorTextColor() {
        return indicatorTextColor;
    }

    public void setIndicatorTextColor(int indicatorTextColor) {
        this.indicatorTextColor = indicatorTextColor;
    }

    public int getIndicatorTextMargin() {
        return indicatorTextMargin;
    }

    public void setIndicatorTextMargin(int indicatorTextMargin) {
        this.indicatorTextMargin = indicatorTextMargin;
    }

    public String getIndicatorText() {
        return indicatorText;
    }

    public void setIndicatorText(String indicatorText) {
        this.indicatorText = indicatorText;
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    protected boolean onLevelChange(int level) {
        progress = level;
        invalidateSelf();
        if (onFinishListener != null && level >= MAX_PROGRESS) {
            onFinishListener.onFinish();
        }
        return true;
    }

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }
}
