package com.tosslab.jandi.app.utils.image.loader;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.ViewPropertyAnimation;
import com.tosslab.jandi.app.utils.image.target.DynamicImageViewTarget;

/**
 * Created by tonyjs on 15. 12. 21..
 */
public class ImageLoader {

    public static final String TAG = ImageLoader.class.getSimpleName();

    private int placeHolder;
    private Drawable placeHolderDrawable;
    private ImageView.ScaleType placeHolderScaleType;
    private ImageView.ScaleType actualImageScaleType;
    private int error;
    private ImageView.ScaleType errorScaleType;
    private ViewPropertyAnimation.Animator animator;
    private int anim = -1;
    private Transformation<Bitmap> transformation;
    private RequestListener<Uri, GlideDrawable> listener;
    private Uri uri;

    ImageLoader() {
    }

    public static ImageLoader newInstance() {
        return new ImageLoader();
    }

    public ImageLoader placeHolder(int placeHolder) {
        return placeHolder(placeHolder, ImageView.ScaleType.FIT_CENTER);
    }

    public ImageLoader placeHolder(int placeHolder, ImageView.ScaleType scaleType) {
        this.placeHolder = placeHolder;
        this.placeHolderScaleType = scaleType;
        return this;
    }

    public ImageLoader placeHolder(Drawable placeHolder) {
        return placeHolder(placeHolder, ImageView.ScaleType.FIT_CENTER);
    }

    public ImageLoader placeHolder(Drawable placeHolder, ImageView.ScaleType scaleType) {
        this.placeHolderDrawable = placeHolder;
        this.placeHolderScaleType = scaleType;
        return this;
    }

    public ImageLoader actualImageScaleType(ImageView.ScaleType scaleType) {
        this.actualImageScaleType = scaleType;
        return this;
    }

    public ImageLoader error(int error) {
        return error(error, ImageView.ScaleType.FIT_CENTER);
    }

    public ImageLoader error(int error, ImageView.ScaleType scaleType) {
        this.error = error;
        this.errorScaleType = scaleType;
        return this;
    }

    public ImageLoader animate(int animResId) {
        this.anim = animResId;
        return this;
    }

    public ImageLoader animator(ViewPropertyAnimation.Animator animator) {
        this.animator = animator;
        return this;
    }

    public ImageLoader transformation(Transformation<Bitmap> transformation) {
        this.transformation = transformation;
        return this;
    }

    public ImageLoader listener(RequestListener<Uri, GlideDrawable> listener) {
        this.listener = listener;
        return this;
    }

    public ImageLoader uri(Uri uri) {
        this.uri = uri;
        return this;
    }

    @SuppressWarnings("unchecked")
    public void into(ImageView imageView) {
        DrawableRequestBuilder<Uri> request =
                Glide.with(imageView.getContext())
                        .load(uri)
                        .fitCenter();

        if (placeHolderDrawable != null) {
            request.placeholder(placeHolderDrawable);
        } else if (placeHolder != -1) {
            request.placeholder(placeHolder);
        }

        request.error(error);

        if (transformation != null) {
            request.bitmapTransform(transformation);
        }

        if (anim != -1) {
            request.animate(anim);
        } else if (animator != null) {
            request.animate(animator);
        }

        request.listener(listener)
                .into(DynamicImageViewTarget.newBuilder()
                        .placeHolderScaleType(placeHolderScaleType)
                        .actualImageScaleType(actualImageScaleType)
                        .errorScaleType(errorScaleType)
                        .build(imageView));
//
    }
}
