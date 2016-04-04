package com.tosslab.jandi.app.views.spannable;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ReplacementSpan;
import android.util.DisplayMetrics;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;

/**
 * Created by tonyjs on 16. 3. 17..
 */
public class OwnerSpannable extends ReplacementSpan {

    private static final int DEFAULT_MARGIN_LEFT = 3;

    private String ownerText;
    private int textColor;
    private float textSize;
    private int extraForCenterVertical;
    private int paddingVertical;
    private int paddingHorizontal;
    private Drawable background;
    private int marginLeftDp;
    public OwnerSpannable(String ownerText, int marginLeftDp) {
        this.ownerText = ownerText;

        Resources resources = JandiApplication.getContext().getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        paddingVertical = (int) (displayMetrics.density * 2);
        paddingHorizontal = (int) (displayMetrics.density * 6);
        extraForCenterVertical = (int) (displayMetrics.density * 1);
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
        return (int) ((paddingHorizontal * 2) + paint.measureText(ownerText)) + marginLeftDp;
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
        int layoutWidth = textWidth + (paddingHorizontal * 2);

        int left = (int) x + marginLeftDp;

        background.setBounds(left, top + paddingVertical, left + layoutWidth, bottom - paddingVertical);
        background.draw(canvas);

        float newY = top + ((bottom - top) - (textSize / 2)) - extraForCenterVertical;

        canvas.drawText(ownerText, x + paddingHorizontal + marginLeftDp, newY, paint);

        canvas.restore();

    }
}
