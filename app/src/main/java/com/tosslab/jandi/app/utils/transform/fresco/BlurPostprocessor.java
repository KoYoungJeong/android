package com.tosslab.jandi.app.utils.transform.fresco;

import android.graphics.Bitmap;

import com.facebook.imagepipeline.request.BasePostprocessor;
import com.tosslab.jandi.app.utils.image.BitmapUtil;

/**
 * Created by tonyjs on 15. 12. 10..
 */
public class BlurPostprocessor extends BasePostprocessor {
    @Override
    public String getName() {
        return "Fresco_BlurProcessor";
    }

    @Override
    public void process(Bitmap dest, Bitmap bitmap) {
        bitmap = BitmapUtil.getBlurBitmap(bitmap, 10);
        super.process(dest, bitmap);
    }
}
