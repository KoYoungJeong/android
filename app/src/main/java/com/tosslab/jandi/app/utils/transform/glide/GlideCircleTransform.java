package com.tosslab.jandi.app.utils.transform.glide;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.tosslab.jandi.app.utils.BitmapUtil;

/**
 * Created by justinygchoi on 14. 12. 5..
 */
public class GlideCircleTransform extends BitmapTransformation {
    private Context context;

    public GlideCircleTransform(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap source, int outWidth, int outHeight) {
        return BitmapUtil.getCircularBitmap(source);
    }

    @Override
    public String getId() {
        return "Glide_Circle_Transformation";
    }

}
