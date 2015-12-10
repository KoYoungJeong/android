package com.tosslab.jandi.app.views.spannable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;

/**
 * Created by tee on 15. 12. 7..
 */
public class MarkdownSpannable extends ReplacementSpan {

    private final String markdownString;
    private final float textSize;
    private String drawText;

    private boolean isItalic = false;
    private boolean isBold = false;
    private boolean isStrike = false;

    private int maxWidth = -1;

    public MarkdownSpannable(String markdownString, float textSize) {
        this.markdownString = markdownString;
        this.textSize = textSize;
        this.drawText = this.markdownString;
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
        paint.getTextBounds(markdownString, 0, markdownString.length(), textRect);
        if (maxWidth > 0 && textRect.width() > maxWidth) {
            textRect.right = maxWidth - textRect.left;
            if (TextUtils.equals(markdownString, drawText)) {
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
        paint.setColor(0xFFFFFFFF);

        Typeface typeface = null;

        canvas.drawRect(x, top, x + textRect.width(), bottom, paint);

        paint.setColor(0xFF000000);

        if (isBold && !isItalic) {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
            paint.setTypeface(typeface);
        }
        if (isItalic && !isBold) {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC);
            paint.setTypeface(typeface);
        }

        if (isItalic && isBold) {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC);
            paint.setTypeface(typeface);
        }

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x + textRect.width(), y);

        canvas.drawTextOnPath(drawText, path, 0, 0, paint);

        if (isStrike) {
            canvas.drawLine(textRect.width() / 10, (textRect.height() - textRect.height() / 10),
                    (textRect.width() - textRect.width() / 10), (textRect.height() - textRect.height() / 10), paint);
        }

        canvas.restore();
    }

    public void setIsItalic(boolean isItalic) {
        this.isItalic = isItalic;
    }

    public void setIsBold(boolean isBold) {
        this.isBold = isBold;
    }

    public void setIsStrike(boolean isStrike) {
        this.isStrike = isStrike;
    }
}