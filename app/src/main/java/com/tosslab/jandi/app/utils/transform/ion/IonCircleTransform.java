package com.tosslab.jandi.app.utils.transform.ion;

import android.graphics.Bitmap;

import com.tosslab.jandi.app.utils.BitmapUtil;

/**
 * Created by Steve SeongUg Jung on 15. 2. 11..
 */
public class IonCircleTransform implements com.koushikdutta.ion.bitmap.Transform {
    @Override
    public Bitmap transform(Bitmap source) {
        return BitmapUtil.getCircularBitmap(source);
    }

    @Override
    public String key() {
        return "Ion_Circle_Transform";
    }
}
