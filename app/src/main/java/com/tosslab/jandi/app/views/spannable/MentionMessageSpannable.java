package com.tosslab.jandi.app.views.spannable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

/**
 * Created by tee on 15. 8. 6..
 */
public class MentionMessageSpannable extends ReplacementSpan {

    protected TextView textView;

    private int maxWidth = -1;

    public MentionMessageSpannable(Context context, String name, float pxSize,
                                   int textColor, int backgroundColor) {

        textView = new TextView(context);
        textView.setText(name);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, pxSize);
        textView.setBackgroundColor(backgroundColor);
        textView.setTextColor(textColor);
        textView.setPadding(0, 0, 0, 0);

        prepareView();

    }

    public void setViewMaxWidthSize(int px) {
        maxWidth = px;
        prepareView();
    }

    private void prepareView() {

        int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        textView.measure(widthSpec, heightSpec);

        if (maxWidth > 0) {
            textView.setMaxWidth(maxWidth);
        }

        textView.setMaxLines(1);
        textView.setEllipsize(TextUtils.TruncateAt.END);

    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return textView.getMeasuredWidth();
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        // http://flavienlaurent.com/blog/2014/01/31/spans/ 참조할것

        canvas.save();

        canvas.translate(x, bottom - textView.getMeasuredHeight());
        textView.draw(canvas);

        canvas.restore();

    }

}
