package com.tosslab.jandi.app.views.spannable;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ReplacementSpan;
import android.util.DisplayMetrics;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 16. 3. 17..
 */
public class OwnerSpannable extends ReplacementSpan {

    private static final int DEFAULT_MARGIN_LEFT = 3;

    private String ownerText;
    private int textColor;
    private float textSize;
    private int padding;
    private Drawable background;
    private int marginLeftDp;
    public OwnerSpannable(String ownerText, int marginLeftDp) {
        this.ownerText = ownerText;

        Resources resources = JandiApplication.getContext().getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        padding = (int) (displayMetrics.density * 6);
        textSize = displayMetrics.scaledDensity * 11f;
        textColor = resources.getColor(R.color.jandi_owner_badge_text);
        background = resources.getDrawable(R.drawable.admin_bg);
        this.marginLeftDp = (int) (marginLeftDp <= 0
                        ? displayMetrics.density * DEFAULT_MARGIN_LEFT
                        : displayMetrics.density * marginLeftDp);
    }

    @Override
    public int getSize(Paint paint,
                       CharSequence text,
                       int start, int end,
                       Paint.FontMetricsInt fm) {
        paint.setTextSize(textSize);
        return (int) ((padding * 2) + paint.measureText(ownerText)) + marginLeftDp;
    }

    @Override
    public void draw(Canvas canvas,
                     CharSequence text,
                     int start, int end, float x, int top, int y, int bottom,
                     Paint paint) {

        canvas.save();

        paint.setTextSize(textSize);
        paint.setColor(textColor);

        int textWidth = (int) paint.measureText(ownerText);
        int layoutWidth = textWidth + (padding * 2);
        LogUtil.e("tony", "textWidth - " + textWidth + " layoutWidth = " + layoutWidth);
//        LogUtil.e("tony", String.format("start = %d. end = %d. x = %f. top = %d. y = %d, bottom = %d", start, end, x, top, y, bottom));

        int left = (int) x + marginLeftDp;
        background.setBounds(left, top, (int) left + layoutWidth, bottom);
        background.draw(canvas);

        float newY = top + ((bottom - top) - (textSize / 2));

//        int halfOfLayoutWidth = layoutWidth / 2;
//        int halfOfTextWidth = textWidth / 2;
//        LogUtil.d("tony", "halfOfLayoutWidth - " + halfOfLayoutWidth + " halfOfTextWidth - " + halfOfTextWidth);
        canvas.drawText(ownerText, x + padding + marginLeftDp, newY, paint);

        canvas.restore();

    }
}
