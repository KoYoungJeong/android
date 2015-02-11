package com.tosslab.jandi.app.utils;

import android.graphics.Bitmap;

/**
 * Created by Steve SeongUg Jung on 15. 2. 11..
 */
public class IonCircleTransform implements com.koushikdutta.ion.bitmap.Transform {
    @Override
    public Bitmap transform(Bitmap source) {
        return BitmapUtil.getCircularBitmapImage(source);
    }

    @Override
    public String key() {
        return "Ion_Circle_Transform";
    }
}
