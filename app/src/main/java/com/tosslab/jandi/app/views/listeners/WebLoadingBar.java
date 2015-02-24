package com.tosslab.jandi.app.views.listeners;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.tosslab.jandi.app.R;

/**
 * Created by Steve SeongUg Jung on 15. 2. 24..
 */
public class WebLoadingBar extends View {

    private final Paint paint;
    private int progress;
    private int width;
    private int height;
    private int max = 100;

    public WebLoadingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();

        paint.setColor(getResources().getColor(R.color.file_name));
    }

    @Override
    protected void onDraw(Canvas canvas) {


        canvas.save();
        canvas.drawRect(0, 0, (progress * width) / max, height, paint);
        canvas.restore();


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);


    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }

    public void setMax(int max) {
        this.max = max;
    }
}
