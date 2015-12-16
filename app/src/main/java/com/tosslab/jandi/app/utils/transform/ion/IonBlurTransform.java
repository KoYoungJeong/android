package com.tosslab.jandi.app.utils.transform.ion;

import android.graphics.Bitmap;

import com.tosslab.jandi.app.utils.image.ImageUtil;

/**
 * Created by Steve SeongUg Jung on 15. 2. 11..
 */
class IonBlurTransform implements com.koushikdutta.ion.bitmap.Transform {

    public static final int DEFAULT_RADIUS = 10;

    @Override
    public Bitmap transform(Bitmap source) {
        return ImageUtil.getBlurBitmap(source, DEFAULT_RADIUS);
    }

    @Override
    public String key() {
        return "Ion_Blur_Transform";
    }
}
