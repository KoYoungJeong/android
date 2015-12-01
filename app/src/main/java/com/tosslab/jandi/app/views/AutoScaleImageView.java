package com.tosslab.jandi.app.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.BitmapTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 15. 06. 16..
 */
public class AutoScaleImageView extends ImageView {
    public static final String TAG = AutoScaleImageView.class.getSimpleName();
    public static final int MAX_WIDTH_WHEN_VERTICAL_IMAGE = 160;
    public static final int MAX_HEIGHT_WHEN_VERTICAL_IMAGE = 284;
    public static final int DEFAULT_WIDTH = 213;
    public static final int DEFAULT_HEIGHT = 120;

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

    @Override
    protected void onDetachedFromWindow() {
        if (imageSpec != null) {
            Request request = imageSpec.getRequest();
            if (request != null) {
                request.clear();
                request = null;
            }
            imageSpec = null;
        }
        super.onDetachedFromWindow();
    }

    public void load(String url, int width, int height, int orientation) {
        load(url, width, height, orientation, false);
    }

    public void load(String url, int width, int height, int orientation, boolean centerCrop) {
        if (imageSpec != null && !imageSpec.getUrl().equals(url) && imageSpec.getRequest().isRunning()) {
            LogUtil.i(TAG, "previous request is running so clear.");
            imageSpec.getRequest().clear();
            imageSpec.setRequest(null);
        }

        imageSpec = getImageSpec(url, width, height, orientation);

        BaseTarget requestTarget = Glide.with(getContext())
                .load(url)
                .asBitmap()
                .placeholder(new ColorDrawable(Color.parseColor("#eaeaea")))
                .centerCrop()
                .into(getTarget(centerCrop, imageSpec.getWidth(), imageSpec.getHeight()));
        imageSpec.setRequest(requestTarget.getRequest());

        requestLayout();
    }

    private BaseTarget getTarget(boolean centerCrop, int width, int height) {
        if (centerCrop) {
            return new BitmapImageViewTarget(this) {
                @Override
                public void onResourceReady(Bitmap resource,
                                            GlideAnimation<? super Bitmap> glideAnimation) {
                    if (!isRequestCancelled(getRequest()) && isValidateResource(resource)) {
                        super.onResourceReady(resource, glideAnimation);
                        setResource(resource);
                    }
                }
            };
        } else {
            return new SimpleTarget<Bitmap>(width, height) {

                @Override
                public void onLoadStarted(Drawable placeholder) {
                    setImageDrawable(placeholder);
                }

                @Override
                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                    setImageDrawable(errorDrawable);
                }

                @Override
                public void onLoadCleared(Drawable placeholder) {
                    setImageDrawable(placeholder);
                }

                @Override
                public void onResourceReady(Bitmap resource,
                                            GlideAnimation<? super Bitmap> glideAnimation) {
                    if (!isRequestCancelled(getRequest()) && isValidateResource(resource)) {
                        setImageBitmap(resource);
                    }
                }
            };
        }
    }

    public void load(String url) {
        load(url,
                getDpFromPixel(DEFAULT_WIDTH), getDpFromPixel(DEFAULT_HEIGHT),
                ExifInterface.ORIENTATION_UNDEFINED);
    }

    private ImageSpec getImageSpec(String url, int width, int height, int orientation) {
        // Vertical Image.
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90
                || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            int temp = height;
            height = width;
            width = temp;
        }

        float ratio = height / (float) width;
        LogUtil.i("ImageSize", String.format("%d, %d, %f", width, height, ratio));

        if (height > width) {
            width = getDpFromPixel(MAX_WIDTH_WHEN_VERTICAL_IMAGE);
            height = Math.min((int) (width * ratio), getDpFromPixel(MAX_HEIGHT_WHEN_VERTICAL_IMAGE));
        } else if (height == width) {
            width = getDpFromPixel(DEFAULT_WIDTH);
            height = getDpFromPixel(DEFAULT_WIDTH);
        } else {
            width = getDpFromPixel(DEFAULT_WIDTH);
            height = (int) (width * ratio);
        }

        return new ImageSpec(url, width, height, orientation);
    }

    private boolean isRequestCancelled(Request request) {
        return request == null || request.isCancelled();
    }

    private boolean isValidateResource(Bitmap resource) {
        return resource != null && !resource.isRecycled();
    }

    private int getDpFromPixel(int pixel) {
        return (int) (pixel * getContext().getResources().getDisplayMetrics().density);
    }

    private static class ImageSpec {
        private String url;
        private int width;
        private int height;
        private int orientation;
        private Request request;

        public ImageSpec(String url, int width, int height, int orientation) {
            this.url = url;
            this.width = width;
            this.height = height;
            this.orientation = orientation;
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

        public void setRequest(Request request) {
            this.request = request;
        }
    }

}
