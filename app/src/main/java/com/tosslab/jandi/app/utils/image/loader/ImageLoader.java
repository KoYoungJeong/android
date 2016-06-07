package com.tosslab.jandi.app.utils.image.loader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.ViewPropertyAnimation;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.utils.image.target.DynamicImageViewTarget;

/**
 * Created by tonyjs on 15. 12. 21..
 */
public class ImageLoader {

    public static final String TAG = ImageLoader.class.getSimpleName();

    private int backgroundColor = Integer.MAX_VALUE;
    private int placeHolder;
    private Drawable placeHolderDrawable;
    private ImageView.ScaleType placeHolderScaleType;
    private ImageView.ScaleType actualImageScaleType;
    private int error;
    private ImageView.ScaleType errorScaleType;
    private ViewPropertyAnimation.Animator animator;
    private int anim = -1;
    private Transformation<Bitmap> transformation;
    private boolean blockNetworking = false;
    private RequestListener<Uri, GlideDrawable> listener;
    private Uri uri;

    ImageLoader() {
    }

    public static ImageLoader newInstance() {
        return new ImageLoader();
    }

    public ImageLoader backgroundColor(int color) {
        this.backgroundColor = color;
        return this;
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

    public ImageLoader blockNetworking(boolean blockNetworking) {
        this.blockNetworking = blockNetworking;
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
        Context context = getAvailableContext(imageView);
        if (context == null) {
            return;
        }

        if (backgroundColor != Integer.MAX_VALUE) {
            imageView.setBackgroundColor(backgroundColor);
        }

        DrawableRequestBuilder<Uri> request = getRequest(imageView).fitCenter();

        request.diskCacheStrategy(DiskCacheStrategy.ALL);

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
        } else {
            // crossFade 가 동작하면 fitCenter 가 정상동작하지 않는다.(TransitionDrawable issue)
            request.animate(view -> {
                view.setAlpha(0.0f);
                view.animate()
                        .alpha(1.0f)
                        .setDuration(300);
            });
        }

        request.listener(listener)
                .into(DynamicImageViewTarget.newBuilder()
                        .placeHolderScaleType(placeHolderScaleType)
                        .actualImageScaleType(actualImageScaleType)
                        .errorScaleType(errorScaleType)
                        .build(imageView));
    }

    private DrawableTypeRequest<Uri> getRequest(ImageView imageView) {
        if (blockNetworking) {
            return Glide.with(imageView.getContext())
                    // cache 되어 있는지 확인하기 위해 네트워킹 작업이 실행되면 exception 발생시킨다.
                    .using(new ThrowIOExceptionStreamLoader<Uri>())
                    .load(uri);
        } else {
            return Glide.with(imageView.getContext()).load(uri);
        }
    }

    /**
     * Glide 이미지 로드 할 때 넘겨준 Context 가 Activity 인 경우(#Glide.with(context))
     * #Build.VERSION_CODES.JELLY_BEAN_MR1 이상인 디바이스에서 사용할 수 있는
     * #Activity.isDestroyed() 메소드로 액티비티의 종료를 판단해서 exception 을 발생시킨다.
     *
     * #Build.VERSION_CODES.JELLY_BEAN_MR1 미만인 단말은 무조건 application context 를 활용하도록 하고
     * 이상인 단말에서는 #Activity.isDestroyed() 로 판단해서 activity 가 destroy 된 경우 null 을 리턴하도록 구현함.
     * @param imageView
     * @return
     */
    @Nullable
    private Context getAvailableContext(ImageView imageView) {
        Context context = imageView.getContext();
        if (context == null) {
            return null;
        }

        boolean isVersionLowerThan17 = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1;
        if (isVersionLowerThan17) {
            return JandiApplication.getContext();
        }

        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || isDestroyed(activity)) {
                return null;
            }

            return context;
        } else {
            return JandiApplication.getContext();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private boolean isDestroyed(Activity activity) {
        return activity.isDestroyed();
    }
}
