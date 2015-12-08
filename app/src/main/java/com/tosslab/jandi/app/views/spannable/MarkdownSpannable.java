package com.tosslab.jandi.app.views.spannable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;

import com.tosslab.jandi.app.ui.commonviewmodels.markdown.vo.MarkdownVO;

/**
 * Created by tee on 15. 12. 7..
 */
public class MarkdownSpannable extends ReplacementSpan {

    private final String markdownString;
    private final float textSize;
    private final MarkdownVO.TYPE type;
    private String drawText;

    private int maxWidth = -1;

    public MarkdownSpannable(String markdownString, float textSize, MarkdownVO.TYPE type) {
        this.markdownString = markdownString;
        this.textSize = textSize;
        this.type = type;
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
        if (type.equals(MarkdownVO.TYPE.BOLD)) {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
            paint.setTypeface(typeface);
        } else if (type.equals(MarkdownVO.TYPE.ITALIC)) {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC);
            paint.setTypeface(typeface);
        } else if (type.equals(MarkdownVO.TYPE.ITALICBOLD)) {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC);
            paint.setTypeface(typeface);
        } else if (type.equals(MarkdownVO.TYPE.STRIKE)) {
            paint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        }

        canvas.drawRect(x, top, x + textRect.width(), bottom, paint);
        paint.setColor(0xFF000000);

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x + textRect.width(), y);
        canvas.drawTextOnPath(drawText, path, 0, 0, paint);

        canvas.restore();
    }

}