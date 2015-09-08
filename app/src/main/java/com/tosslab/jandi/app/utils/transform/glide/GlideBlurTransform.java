package com.tosslab.jandi.app.utils.transform.glide;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.tosslab.jandi.app.utils.BitmapUtil;

/**
 * Created by tonyjs on 15. 9. 8..
 */
public class GlideBlurTransform extends BitmapTransformation {
    private int radius;

    public GlideBlurTransform(Context context) {
        this(context, 2);
    }

    public GlideBlurTransform(Context context, int radius) {
        super(context);
        this.radius = radius;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return BitmapUtil.getBlurBitmap(toTransform, radius);
    }

    @Override
    public String getId() {
        return "Glide_Blur_Transformation";
    }
}
