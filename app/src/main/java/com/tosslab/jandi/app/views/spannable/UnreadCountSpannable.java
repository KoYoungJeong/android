package com.tosslab.jandi.app.views.spannable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.text.style.ImageSpan;
import android.util.TypedValue;

import com.tosslab.jandi.app.R;

/**
 * Created by Steve SeongUg Jung on 15. 5. 7..
 */
public class UnreadCountSpannable extends ImageSpan {

    private UnreadCountSpannable(Drawable drawable) {
        super(drawable);
    }

    public static UnreadCountSpannable createUnreadCountSpannable(Context context, String count) {

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.jandi_img_read_unread);
        byte[] ninePatchChunk = bitmap.getNinePatchChunk();
        NinePatch ninePatch = new NinePatch(bitmap, ninePatchChunk);

        UnreadDrawable ninePatchDrawable = new UnreadDrawable(context.getResources(), ninePatch, count);

        return new UnreadCountSpannable(ninePatchDrawable);
    }


    private static class UnreadDrawable extends NinePatchDrawable {
        private final Resources res;
        private final String count;
        private Paint paint;
        private Rect bounds;

        public UnreadDrawable(Resources res, NinePatch patch, String count) {
            super(res, patch);
            this.res = res;
            this.count = count;

            float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f, res.getDisplayMetrics());

            paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(textSize);
            paint.setFakeBoldText(true);
            paint.setTextAlign(Paint.Align.CENTER);

            Rect bounds = new Rect();
            paint.getTextBounds(count, 0, count.length(), bounds);


            float contentHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, res.getDisplayMetrics());
            float contentWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3.67f, res.getDisplayMetrics());
            float paddingHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, res.getDisplayMetrics());
            float paddingWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7.3f, res.getDisplayMetrics());

            int height = (int) (paddingHeight + Math.max(bounds.height(), contentHeight));
            int width = (int) (paddingWidth + Math.max(bounds.width(), contentWidth));

            setBounds(0, 0, width, height);

        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);

            if (bounds == null) {
                bounds = new Rect();
            }
            paint.getTextBounds(count, 0, count.length(), bounds);

            Rect drawableSize = getBounds();

            canvas.drawText(count, drawableSize.width() / 2, drawableSize.height() / 2 + bounds.height() / 2, paint);
        }
    }
}
