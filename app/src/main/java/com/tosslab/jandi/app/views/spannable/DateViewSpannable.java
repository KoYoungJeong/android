package com.tosslab.jandi.app.views.spannable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

import com.tosslab.jandi.app.R;

/**
 * Created by Steve SeongUg Jung on 15. 3. 12..
 */
public class DateViewSpannable extends ReplacementSpan {

    private final String date;
    private final int textColor;
    private final int textSize;

    public DateViewSpannable(Context context, String date) {

        super();
        this.date = date;
        Resources resources = context.getResources();
        textColor = resources.getColor(R.color.jandi_file_detail_date);
        textSize = resources.getDimensionPixelSize(R.dimen.jandi_file_detail_comment_date_text);


    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        paint.setTextSize(textSize);
        return (int) paint.measureText(date);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        // http://flavienlaurent.com/blog/2014/01/31/spans/ 참조할것

        canvas.save();

        paint.setTextSize(textSize);
        paint.setColor(textColor);
        canvas.drawText(date, x, y, paint);

        canvas.restore();

    }
}
