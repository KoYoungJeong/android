package com.tosslab.jandi.app.views.spannable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;

/**
 * Created by tee on 15. 8. 6..
 */
public class MentionMessageSpannable extends ReplacementSpan {

    private final String name;
    private final float textSize;
    private final int textColor;
    private final int backgroundColor;

    private String drawText;

    private int maxWidth = -1;

    public MentionMessageSpannable(String entityName, float textSize,
                                   int textColor, int backgroundColor) {
        this.name = entityName;
        this.textSize = textSize;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.drawText = this.name;
    }

    public void setViewMaxWidthSize(int px) {
        maxWidth = px;
    }


    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        paint.setTextSize(textSize);
        return getTextWidth(paint).width();
    }

    private Rect getTextWidth(Paint paint) {
        paint.setTextSize(textSize);
        Rect textRect = new Rect();
        paint.getTextBounds(name, 0, name.length(), textRect);
        if (maxWidth > 0 && textRect.width() > maxWidth) {
            textRect.right = maxWidth - textRect.left;

            if (TextUtils.equals(name, drawText)) {
                Rect drawRect = new Rect();
                paint.getTextBounds(drawText, 0, drawText.length() - 1, drawRect);
                while (drawText.length() > 2 && textRect.width() < (drawRect.width() + 10)) {
                    drawText = drawText.substring(0, drawText.length() - 2);
                    paint.getTextBounds(drawText, 0, drawText.length(), drawRect);
                }
                drawText = drawText + "...";
            }

        }
        return textRect;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        // http://flavienlaurent.com/blog/2014/01/31/spans/ 참조할것

        canvas.save();

        paint.setTextSize(textSize);

        Rect textRect = getTextWidth(paint);

        paint.setColor(backgroundColor);
        canvas.drawRect(x, top, x + textRect.width(), bottom, paint);

        paint.setColor(textColor);
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x + textRect.width(), y);
        canvas.drawTextOnPath(drawText, path, 0, 0, paint);

        canvas.restore();

    }

}
