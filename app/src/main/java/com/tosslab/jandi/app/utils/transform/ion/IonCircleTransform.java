package com.tosslab.jandi.app.utils.transform.ion;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.transform.TransformConfig;

/**
 * Created by Steve SeongUg Jung on 15. 2. 11..
 */
public class IonCircleTransform implements com.koushikdutta.ion.bitmap.Transform {
    private float lineWidth;
    private int lineColor;
    private int bgColor;

    public IonCircleTransform() {
        this(TransformConfig.DEFAULT_CIRCLE_LINE_WIDTH, TransformConfig.DEFAULT_CIRCLE_LINE_COLOR);
    }

    public IonCircleTransform(float lineWidth, int lineColor) {
        this(lineWidth, lineColor, TransformConfig.DEFAULT_CIRCLE_BG_COLOR);
    }

    public IonCircleTransform(float lineWidth, int lineColor, int bgColor) {
        this.lineWidth = lineWidth;
        this.lineColor = lineColor;
        this.bgColor = bgColor;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        return BitmapUtil.getCircularBitmap(source, lineWidth, lineColor, bgColor);
    }

    @Override
    public String key() {
        return "Ion_Circle_Transform";
    }
}
