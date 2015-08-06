package com.tosslab.jandi.app.views.spannable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

/**
 * Created by tee on 15. 8. 6..
 */
public class MentionMessageSpannable extends ReplacementSpan {

    protected TextView tvDate;
    protected int entityId;

    public MentionMessageSpannable(Context context, String name, int entityId, float pxSize) {
        this.entityId = entityId;
        tvDate = new TextView(context);
        tvDate.setText(name);
        tvDate.setTextColor(0xFF00a6e9);
        tvDate.setBackgroundColor(0xFFdaf2ff);
        tvDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, pxSize);

        prepareView();
    }

    public MentionMessageSpannable(Context context, String name, int entityId, float pxSize, int textColor, int backgroundColor) {
        this.entityId = entityId;

        tvDate = new TextView(context);
        tvDate.setText(name);
        tvDate.setTextColor(textColor);
        if (backgroundColor != -1) {
            tvDate.setBackgroundColor(backgroundColor);
        }
        tvDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, pxSize);

        prepareView();
    }


    private void prepareView() {

        int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        tvDate.measure(widthSpec, heightSpec);
        tvDate.layout(0, 0, tvDate.getMeasuredWidth(), tvDate.getMeasuredHeight());

    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return tvDate.getWidth();
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {

        canvas.save();

        int padding = (bottom - top - tvDate.getBottom()) / 2;
        canvas.translate(x, bottom - tvDate.getBottom() - padding);
        tvDate.draw(canvas);

        canvas.restore();

    }

}
