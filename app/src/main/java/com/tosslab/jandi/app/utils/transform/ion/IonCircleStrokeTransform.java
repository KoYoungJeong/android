package com.tosslab.jandi.app.utils.transform.ion;

import android.graphics.Bitmap;

import com.tosslab.jandi.app.utils.BitmapUtil;

/**
 * Created by Steve SeongUg Jung on 15. 2. 11..
 */
public class IonCircleStrokeTransform implements com.koushikdutta.ion.bitmap.Transform {
    public static final int NONE_COLOR = -1;
    private float lineWidth;
    private int lineColor;
    private int bgColor = NONE_COLOR;
    public IonCircleStrokeTransform() {
    }

    public IonCircleStrokeTransform(float lineWidth, int lineColor) {
        this(lineWidth, lineColor, NONE_COLOR);
    }

    public IonCircleStrokeTransform(float lineWidth, int lineColor, int bgColor) {
        this.lineWidth = lineWidth;
        this.lineColor = lineColor;
        this.bgColor = bgColor;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        if (bgColor != NONE_COLOR) {
            return BitmapUtil.getCircularBitmapWithLineAndBG(source, lineWidth, lineColor, bgColor);
        }

        return BitmapUtil.getCircularBitmapWithLine(source, lineWidth, lineColor);
    }

    @Override
    public String key() {
        return "Ion_Circle_Stroke_Transform";
    }
}
