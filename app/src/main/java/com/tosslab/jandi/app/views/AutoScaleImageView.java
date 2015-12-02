package com.tosslab.jandi.app.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 15. 06. 16..
 */
public class AutoScaleImageView extends ImageView {
    public static final String TAG = AutoScaleImageView.class.getSimpleName();

    public static final int MAX_WIDTH_WHEN_VERTICAL_IMAGE = 160;
    public static final int MAX_HEIGHT_WHEN_VERTICAL_IMAGE = 284;

    public static final int MAX_WIDTH = 213;
    public static final int MAX_HEIGHT = 120;

    public static final int MIN_SIZE = 46;
    public static final int SMALL_SIZE = 90;

    private ImageSpec imageSpec;

    public AutoScaleImageView(Context context) {
        super(context);
    }

    public AutoScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoScaleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (imageSpec != null) {
            setMeasuredDimension(imageSpec.getWidth(), imageSpec.getHeight());
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void load(String url) {
        LogUtil.d(TAG, "extrainfo is empty.");
        int width = getPixelFromDp(MAX_WIDTH);
        int height = getPixelFromDp(MAX_HEIGHT);
        int orientation = ExifInterface.ORIENTATION_UNDEFINED;
        load(url, width, height, orientation);
    }

    public void load(String url, int width, int height, int orientation) {
        LogUtil.d(TAG, "has extrainfo.");

        if (imageSpec != null) {
            if (imageSpec.getUrl().equals(url)) {
                LogUtil.d(TAG, "same request is running - " + imageSpec.getRequest().isRunning());
                return;
            } else {
                stopPreviousRequestIfRunning();
            }
        }

        LogUtil.e(TAG, String.format("%d, %d, %d, %f", width, height, orientation, height / (float) width));

        imageSpec = getImageSpec(url, width, height, orientation);
        requestLayout();
        getParent().requestLayout();

        BitmapRequestBuilder<String, Bitmap> requestBuilder = Glide.with(getContext())
                .load(url)
                .asBitmap();

        setScaleType(ScaleType.FIT_XY);

        LogUtil.e(TAG, imageSpec.toString());
        switch (imageSpec.getType()) {
            case SMALL:
                requestBuilder.fitCenter();
                break;
            case LONG_HORIZONTAL:
                requestBuilder.centerCrop();
                requestBuilder.override(imageSpec.getWidth(), imageSpec.getHeight());
                break;
            case LONG_VERTICAL:
                requestBuilder.centerCrop();
                requestBuilder.override(imageSpec.getWidth(), imageSpec.getHeight());
                break;
            case VERTICAL:
                requestBuilder.centerCrop();
                requestBuilder.override(imageSpec.getWidth(), imageSpec.getHeight());
                break;
            case HORIZONTAL:
                requestBuilder.centerCrop();
                requestBuilder.override(imageSpec.getWidth(), imageSpec.getHeight());
                break;
            case SQUARE:
                requestBuilder.fitCenter();
                requestBuilder.override(imageSpec.getWidth(), imageSpec.getHeight());
                break;
            case MAX:
            default:
                requestBuilder.centerCrop();
                break;
        }

        requestBuilder.placeholder(R.drawable.image_preview_download);
        BaseTarget requestTarget = requestBuilder.into(new DefaultImageViewTarget(this));

        imageSpec.setRequest(requestTarget.getRequest());
    }

    private ImageSpec getImageSpec(String url, int width, int height, int orientation) {
        // Vertical Image.
        if (isVerticalPhoto(orientation)) {
            int temp = height;
            height = width;
            width = temp;
        }

        float ratio = height / (float) width;
        LogUtil.i(TAG, String.format("%d, %d, %d, %f", width, height, orientation, ratio));

        if (isSmallSize(width, height)) {
            ImageSpec.Type type = ImageSpec.Type.SMALL;
            int size = getPixelFromDp(SMALL_SIZE);
            return new ImageSpec(url, size, size, orientation, type);
        }

        if (width == height) {
            ImageSpec.Type type = ImageSpec.Type.SQUARE;
            int size = Math.min(width, getPixelFromDp(MAX_WIDTH_WHEN_VERTICAL_IMAGE));
            return new ImageSpec(url, size, size, orientation, type);
        }

        if (width > height) {
            width = Math.min(width, getPixelFromDp(MAX_WIDTH));

            int minSize = getPixelFromDp(MIN_SIZE);
            if (ratio <= (46 / 213f)) {
                ImageSpec.Type type = ImageSpec.Type.LONG_HORIZONTAL;
                height = minSize;
                return new ImageSpec(url, width, height, orientation, type);
            }

            ImageSpec.Type type = ImageSpec.Type.HORIZONTAL;
            height = (int) (width * ratio);
            return new ImageSpec(url, width, height, orientation, type);
        } else {
            height = Math.min(height, getPixelFromDp(MAX_HEIGHT_WHEN_VERTICAL_IMAGE));

            int minSize = getPixelFromDp(MIN_SIZE);
            if (ratio > (284 / 46f)) {
                ImageSpec.Type type = ImageSpec.Type.LONG_VERTICAL;
                width = minSize;
                return new ImageSpec(url, width, height, orientation, type);
            }

            ImageSpec.Type type = ImageSpec.Type.VERTICAL;
            width = Math.min((int) (height / ratio), getPixelFromDp(MAX_WIDTH_WHEN_VERTICAL_IMAGE));
            return new ImageSpec(url, width, height, orientation, type);
        }
    }

    private void stopPreviousRequestIfRunning() {
        boolean running = imageSpec.getRequest().isRunning();
        LogUtil.i(TAG, "different imageSpec passed. running ? " + running);
        if (running) {
            LogUtil.i(TAG, "previous request is running so clear.");
            Request request = imageSpec.getRequest();
            request.clear();
            request = null;
        }
    }

    private boolean isSmallSize(int width, int height) {
        return width <= getPixelFromDp(SMALL_SIZE)
                && height <= getPixelFromDp(SMALL_SIZE);
    }

    private boolean isVerticalPhoto(int orientation) {
        return orientation == ExifInterface.ORIENTATION_ROTATE_90
                || orientation == ExifInterface.ORIENTATION_ROTATE_270;
    }

    private int getPixelFromDp(int dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density);
    }

    public static boolean isRequestCancelled(Request request) {
        return request == null || request.isCancelled();
    }

    public static boolean isValidateResource(Bitmap resource) {
        return resource != null && !resource.isRecycled();
    }

    private static class ImageSpec {
        public enum Type {
            MAX, HORIZONTAL, VERTICAL, LONG_HORIZONTAL, LONG_VERTICAL, SQUARE, SMALL
        }

        private String url;
        private int width;
        private int height;
        private int orientation;
        private Request request;
        private Type type = Type.MAX;

        public ImageSpec(String url, int width, int height, int orientation, Type type) {
            this.url = url;
            this.width = width;
            this.height = height;
            this.orientation = orientation;
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getOrientation() {
            return orientation;
        }

        public Request getRequest() {
            return request;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void setRequest(Request request) {
            this.request = request;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "ImageSpec{" +
                    "url='" + url + '\'' +
                    ", width=" + width +
                    ", height=" + height +
                    ", orientation=" + orientation +
                    ", request=" + request +
                    ", type=" + type +
                    '}';
        }
    }

    private static class DefaultImageViewTarget extends BitmapImageViewTarget {

        private boolean needRoundedCorner = true;

        public DefaultImageViewTarget(ImageView view) {
            this(view, true);
        }

        public DefaultImageViewTarget(ImageView view, boolean needRoundedCorner) {
            super(view);
            this.needRoundedCorner = needRoundedCorner;
        }

        @Override
        public void onResourceReady(Bitmap resource,
                                    GlideAnimation<? super Bitmap> glideAnimation) {
            if (!isRequestCancelled(getRequest()) && isValidateResource(resource)) {
                super.onResourceReady(resource, glideAnimation);
                if (needRoundedCorner) {
                    RoundedBitmapDrawable roundedBitmapDrawable = getRoundedBitmapDrawable(resource, 5);
                    view.setImageDrawable(roundedBitmapDrawable);
                } else {
                    view.setScaleType(ScaleType.FIT_CENTER);
                    view.setImageBitmap(resource);
                }
            }
        }
    }

    private static class SizeSpecifiedTarget extends SimpleTarget<Bitmap> {

        private ImageView imageView;

        public SizeSpecifiedTarget(ImageView imageView, int width, int height) {
            super(width, height);
            this.imageView = imageView;
        }

        @Override
        public void onLoadStarted(Drawable placeholder) {
            imageView.setImageDrawable(placeholder);
        }

        @Override
        public void onLoadFailed(Exception e, Drawable errorDrawable) {
            imageView.setImageDrawable(errorDrawable);
        }

        @Override
        public void onLoadCleared(Drawable placeholder) {
            imageView.setImageDrawable(placeholder);
        }

        @Override
        public void onResourceReady(Bitmap resource,
                                    GlideAnimation<? super Bitmap> glideAnimation) {
            if (!isRequestCancelled(getRequest()) && isValidateResource(resource)) {
                RoundedBitmapDrawable roundedBitmapDrawable = getRoundedBitmapDrawable(resource, 5);
                imageView.setImageDrawable(roundedBitmapDrawable);
//                imageView.setImageBitmap(resource);
            }
        }

    }

    public static RoundedBitmapDrawable getRoundedBitmapDrawable(Bitmap resource, float radius) {
        Resources resources = JandiApplication.getContext().getResources();
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, resource);
        roundedBitmapDrawable.setCornerRadius(radius);
        return roundedBitmapDrawable;
    }
}
