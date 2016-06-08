package com.tosslab.jandi.app.utils.image.target;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

/**
 * Created by tonyjs on 16. 5. 9..
 */
public class DynamicImageViewTarget extends GlideDrawableImageViewTarget {
    public static final String TAG = DynamicImageViewTarget.class.getSimpleName();

    private ImageView.ScaleType placeHolderScaleType = ImageView.ScaleType.FIT_CENTER;
    private ImageView.ScaleType actualImageScaleType = ImageView.ScaleType.FIT_CENTER;
    private ImageView.ScaleType errorScaleType = ImageView.ScaleType.FIT_CENTER;

    private DynamicImageViewTarget(ImageView view,
                                   ImageView.ScaleType placeHolderScaleType,
                                   ImageView.ScaleType actualImageScaleType,
                                   ImageView.ScaleType errorScaleType) {
        super(view);
        this.placeHolderScaleType = placeHolderScaleType;
        this.actualImageScaleType = actualImageScaleType;
        this.errorScaleType = errorScaleType;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public void onLoadStarted(Drawable placeholder) {
        if (placeHolderScaleType != null) {
            view.setScaleType(placeHolderScaleType);
        }
        super.onLoadStarted(placeholder);
    }

    @Override
    public void onLoadFailed(Exception e, Drawable errorDrawable) {
        if (errorScaleType != null) {
            view.setScaleType(errorScaleType);
        }
        Log.e(TAG, Log.getStackTraceString(e));
        super.onLoadFailed(e, errorDrawable);
    }

    @Override
    public void onResourceReady(GlideDrawable resource,
                                GlideAnimation<? super GlideDrawable> animation) {
        if (actualImageScaleType != null) {
            view.setScaleType(actualImageScaleType);
        }
        super.onResourceReady(resource, animation);
    }

    public static final class Builder {
        private ImageView.ScaleType placeHolderScaleType;
        private ImageView.ScaleType actualImageScaleType;
        private ImageView.ScaleType errorScaleType;

        Builder() {
        }

        public Builder placeHolderScaleType(ImageView.ScaleType scaleType) {
            placeHolderScaleType = scaleType;
            return this;
        }

        public Builder actualImageScaleType(ImageView.ScaleType scaleType) {
            actualImageScaleType = scaleType;
            return this;
        }

        public Builder errorScaleType(ImageView.ScaleType scaleType) {
            errorScaleType = scaleType;
            return this;
        }

        public DynamicImageViewTarget build(ImageView imageView) {
            return new DynamicImageViewTarget(imageView,
                    placeHolderScaleType, actualImageScaleType, errorScaleType);
        }
    }

}
