package com.tosslab.jandi.app.utils.transform.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.transform.TransformConfig;

/**
 * Created by justinygchoi on 14. 12. 5..
 */
public class GlideCircleTransform extends BitmapTransformation {
    public GlideCircleTransform(Context context) {
        super(context);
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap source, int outWidth, int outHeight) {
        return BitmapUtil.getCircularBitmap(source,
                TransformConfig.DEFAULT_CIRCLE_LINE_WIDTH,
                TransformConfig.DEFAULT_CIRCLE_LINE_COLOR,
                TransformConfig.DEFAULT_CIRCLE_BG_COLOR);
    }

    @Override
    public String getId() {
        return "Glide_Circle_Transformation";
    }

}
