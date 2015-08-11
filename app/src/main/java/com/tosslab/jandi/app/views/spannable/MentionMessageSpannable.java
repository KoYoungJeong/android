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

    protected TextView textView;


    public MentionMessageSpannable(Context context, String name, float pxSize,
                                   int textColor, int backgroundColor) {

        textView = new TextView(context);
        textView.setText(name);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, pxSize);
        textView.setBackgroundColor(backgroundColor);
        textView.setTextColor(textColor);

        prepareView();

    }


    private void prepareView() {

        int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        textView.measure(widthSpec, heightSpec);
        textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());

    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return textView.getWidth();
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {

        canvas.save();

        int padding = (bottom - top - textView.getBottom()) / 2;
        canvas.translate(x, bottom - textView.getBottom() - padding);
        textView.draw(canvas);

        canvas.restore();

    }

}
