package com.tosslab.jandi.app.utils.image.transform;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.tosslab.jandi.app.utils.transform.TransformConfig;

/**
 * Created by tonyjs on 16. 5. 9..
 */
public class JandiProfileTransform implements Transformation<Bitmap> {

    private BitmapPool bitmapPool;

    private float borderWidth;
    private int borderColor = -1;
    private int backgroundColor = -1;

    public JandiProfileTransform(Context context) {
        this(context,
                TransformConfig.DEFAULT_CIRCLE_BORDER_WIDTH,
                TransformConfig.DEFAULT_CIRCLE_BORDER_COLOR,
                TransformConfig.DEFAULT_CIRCLE_BG_COLOR);
    }

    public JandiProfileTransform(Context context,
                                 float borderWidth, int borderColor, int backgroundColor) {
        this(Glide.get(context).getBitmapPool(),
                borderWidth,
                borderColor,
                backgroundColor);
    }

    public JandiProfileTransform(BitmapPool pool,
                                 float borderWidth, int borderColor, int bgColor) {
        this.bitmapPool = pool;
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
        this.backgroundColor = bgColor;
    }

    @Override
    public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
        Bitmap source = resource.get();
        int size = Math.min(source.getWidth(), source.getHeight());

        int width = (source.getWidth() - size) / 2;
        int height = (source.getHeight() - size) / 2;

        float r = size / 2f;

        Bitmap bitmap = bitmapPool.get(size, size, Bitmap.Config.ARGB_8888);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);

        if (backgroundColor != -1) {
            // Background
            Paint bgPaint = new Paint();
            bgPaint.setFlags(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
            bgPaint.setColor(backgroundColor);
            bgPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(r, r, r - 1, bgPaint);
        }

        Paint paint = new Paint();
        BitmapShader shader =
                new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        if (width != 0 || height != 0) {
            // source isn't square, move viewport to center
            Matrix matrix = new Matrix();
            matrix.setTranslate(-width, -height);
            shader.setLocalMatrix(matrix);
        }
        paint.setShader(shader);
        paint.setAntiAlias(true);

        canvas.drawCircle(r, r, r, paint);

        if (borderColor != -1) {
            Paint borderPaint = new Paint();
            borderPaint.setFlags(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
            borderPaint.setColor(borderColor);
            borderPaint.setStrokeWidth(borderWidth);
            borderPaint.setStyle(Paint.Style.STROKE);
        }

        return BitmapResource.obtain(bitmap, bitmapPool);
    }

    @Override
    public String getId() {
        return "JandiProfileTransform";
    }
}
