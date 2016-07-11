package com.tosslab.jandi.app.views.viewgroup;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import com.tosslab.jandi.app.R;

/**
 * Created by tonyjs on 16. 6. 22..
 */
public class ProgressRelativeLayout extends RelativeLayout {
    private Paint progressPaint;
    private Paint backgroundPaint;
    private int progress;
    private int max;

    public ProgressRelativeLayout(Context context) {
        super(context);
        init();
    }

    public ProgressRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Paint progressPaintTemp = new Paint();
        progressPaintTemp.setAntiAlias(true);
        progressPaint = progressPaintTemp;

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(getResources().getColor(R.color.jandi_press_gray));

        setWillNotDraw(false);
    }

    public void setProgressColor(int color) {
        progressPaint.setColor(color);
        invalidate();
    }

    public void setProgressBackgroundColor(int color) {
        backgroundPaint.setColor(color);
        invalidate();
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        int left = 0;
        int top = 0;
        int right = getWidth();
        int bottom = getHeight();

        if (max <= 0) {
            canvas.drawRect(left, top, right, bottom, backgroundPaint);
        } else {
            float ratio = progress / (float) max;

            int width = right - left;
            int progress = (int) (width * ratio);

            int progressRight = left + progress;
            canvas.drawRect(left, top, progressRight, bottom, progressPaint);
            canvas.drawRect(progressRight, top, right, bottom, backgroundPaint);
        }

        canvas.restore();
    }
}
