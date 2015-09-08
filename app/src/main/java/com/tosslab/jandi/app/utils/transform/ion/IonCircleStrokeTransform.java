package com.tosslab.jandi.app.utils.transform.ion;

import android.graphics.Bitmap;

import com.tosslab.jandi.app.utils.BitmapUtil;

/**
 * Created by Steve SeongUg Jung on 15. 2. 11..
 */
public class IonCircleStrokeTransform implements com.koushikdutta.ion.bitmap.Transform {
    private float lineWidth;
    private int lineColor;

    public IonCircleStrokeTransform() {
    }

    public IonCircleStrokeTransform(float lineWidth, int lineColor) {
        this.lineWidth = lineWidth;
        this.lineColor = lineColor;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        return BitmapUtil.getCircularBitmapImageWithLine(source, lineWidth, lineColor);
    }

    @Override
    public String key() {
        return "Ion_Circle_Stroke_Transform";
    }
}
