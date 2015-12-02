package com.tosslab.jandi.app.utils.transform.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 15. 12. 2..
 */
public class GlideRoundCornerTransform extends BitmapTransformation {
    public static final int PAINT_FLAGS = Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG;
    private static final int CIRCLE_CROP_PAINT_FLAGS = PAINT_FLAGS | Paint.ANTI_ALIAS_FLAG;
    private static final Paint CIRCLE_CROP_SHAPE_PAINT = new Paint(CIRCLE_CROP_PAINT_FLAGS);
    private static final Paint CIRCLE_CROP_BITMAP_PAINT;
    static {
        CIRCLE_CROP_BITMAP_PAINT = new Paint(CIRCLE_CROP_PAINT_FLAGS);
        CIRCLE_CROP_BITMAP_PAINT.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    }

    private int width;
    private int height;
    private boolean crop;
    private Context context;
    public GlideRoundCornerTransform(Context context, int width, int height, boolean crop) {
        super(context);
        this.width = width;
        this.height = height;
        this.crop = crop;
        this.context = context;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        String logFormat = "width = %d, height = %d, outWidth = %d, outHeight = %d";
        LogUtil.e("GlideRoundCornerTransform", String.format(logFormat, width, height, outWidth, outHeight));

        int bitmapWidth = toTransform.getWidth();
        int bitmapHeight = toTransform.getHeight();
        float ratio = bitmapHeight / (float) bitmapWidth;

        int resultWidth = width;
        int resultHeight = height;

        if(bitmapWidth > bitmapHeight) {
            resultWidth = width;
            resultHeight = (int) (resultWidth * ratio);
        } else if (bitmapHeight > bitmapWidth) {
            resultHeight = height;
            resultWidth = (int) (outHeight / ratio);
        }

        LogUtil.e("GlideRoundCornerTransform", String.format("resultWidth=%d, resultHeight=%d", resultWidth, resultHeight));
//        Bitmap result = pool.get(resultWidth, resultHeight, Bitmap.Config.ARGB_8888);
//        if (result == null) {
//            result = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.ARGB_8888);
//        }
//
//        Canvas canvas = new Canvas(result);
//        canvas.drawBitmap(toTransform, 0, 0, new Paint());
        return Bitmap.createScaledBitmap(toTransform, resultWidth, resultHeight, true);
    }

    @Override
    public String getId() {
        return "Glide_Round_Corner_Transform";
    }
}
