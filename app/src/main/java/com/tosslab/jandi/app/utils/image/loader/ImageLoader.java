package com.tosslab.jandi.app.utils.image.loader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.animation.ViewPropertyAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.tosslab.jandi.app.BuildConfig;
import com.tosslab.jandi.app.utils.image.ProgressTarget;
import com.tosslab.jandi.app.utils.image.target.DynamicImageViewTarget;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.io.File;
import java.lang.ref.WeakReference;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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

    private WeakReference<Fragment> fragment;

    ImageLoader() {
    }

    public static ImageLoader newInstance() {
        return new ImageLoader();
    }

    public static void loadFromResources(ImageView imageView, int resId) {
        Context context = imageView.getContext();
        if (context == null) {
            return;
        }

        if (context instanceof Activity
                && ((Activity) context).isFinishing()) {
            return;
        }

        Glide.with(context).load(resId).into(imageView);
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

    public ImageLoader fragment(Fragment fragment) {
        this.fragment = new WeakReference<>(fragment);
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

    public Bitmap getBitmapRect(Context context) throws Exception {
        DrawableTypeRequest<Uri> request = getRequest(context);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;
        int min = Math.min(widthPixels, heightPixels);
        return request.asBitmap().load(uri).into(min, min).get();
    }

    @SuppressWarnings("unchecked")
    public void into(ImageView imageView) {
        DrawableRequestBuilder<Uri> request = getRequestBuilder(imageView);
        if (request == null) return;

        request.listener(listener)
                .into(DynamicImageViewTarget.newBuilder()
                        .placeHolderScaleType(placeHolderScaleType)
                        .actualImageScaleType(actualImageScaleType)
                        .errorScaleType(errorScaleType)
                        .build(imageView));
    }

    @SuppressWarnings("unchecked")
    public void into(SubsamplingScaleImageView imageView) {
        DrawableRequestBuilder<Uri> request = getRequestBuilder(imageView);
        if (request == null) return;

        imageView.setDebug(BuildConfig.DEBUG);

        request.listener(listener)
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        // 이 로직을 타면 이미지의 로컬 캐시가 되었을것이라 기대하고 진행함
                        Observable.fromCallable(() -> {
                            FutureTarget<File> futureTarget = ((DrawableTypeRequest) getRequestBuilder(imageView)).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                            return futureTarget.get();
                        }).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(file -> {
                                    if (imageView != null) {
                                        imageView.setImage(ImageSource.uri(Uri.fromFile(file)).tilingEnabled());
                                    }
                                }, Throwable::printStackTrace);
                    }
                });
    }


    @SuppressWarnings("unchecked")
    public void intoWithProgress(ImageView imageView,
                                 ProgressStarted progressStarted,
                                 ProgressDownloading progressDownloading,
                                 ProgressCompleted progressCompleted,
                                 ProgressPresent progressPresent) {
        DrawableRequestBuilder<Uri> request = getRequestBuilder(imageView);
        if (request == null) return;


        request.listener(listener)
                .into(new ProgressTarget<String, GlideDrawable>(uri.toString(), DynamicImageViewTarget.newBuilder()
                        .placeHolderScaleType(placeHolderScaleType)
                        .actualImageScaleType(actualImageScaleType)
                        .errorScaleType(errorScaleType)
                        .build(imageView)) {
                    @Override
                    protected void onConnecting() {
                        if (progressStarted != null) {
                            progressStarted.onStart();
                        }
                    }

                    @Override
                    protected void onDownloading(long bytesRead, long expectedLength) {
                        if (progressDownloading != null) {
                            progressDownloading.onDownloading(((int) (bytesRead * 100 / expectedLength)));
                        }
                    }

                    @Override
                    protected void onDownloaded() {
                        if (progressCompleted != null) {
                            progressCompleted.onCompleted();
                        }
                    }

                    @Override
                    protected void onDelivered() {
                        if (progressPresent != null) {
                            progressPresent.onPresent();
                        }
                    }
                });
    }

    @SuppressWarnings("unchecked")
    public void intoWithProgress(SubsamplingScaleImageView imageView,
                                 ProgressStarted progressStarted,
                                 ProgressDownloading progressDownloading,
                                 ProgressCompleted progressCompleted,
                                 ProgressPresent progressPresent) {
        DrawableRequestBuilder<Uri> request = getRequestBuilder(imageView);
        if (request == null) return;

        imageView.setDebug(BuildConfig.DEBUG);

        request.listener(listener)
                .into(new ProgressTarget<String, GlideDrawable>(uri.toString(), new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        // 이 로직을 타면 이미지의 로컬 캐시가 되었을것이라 기대하고 진행함
                        Observable.fromCallable(() -> {
                            FutureTarget<File> futureTarget =
                                    ((DrawableTypeRequest) getRequestBuilder(imageView)).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                            return futureTarget.get();
                        }).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(file -> {
                                    if (imageView != null) {
                                        imageView.setImage(ImageSource.uri(Uri.fromFile(file)).tilingEnabled());
                                    }
                                }, Throwable::printStackTrace);
                    }
                }) {
                    @Override
                    protected void onConnecting() {
                        if (progressStarted != null) {
                            progressStarted.onStart();
                        }
                    }

                    @Override
                    protected void onDownloading(long bytesRead, long expectedLength) {
                        if (progressDownloading != null) {
                            progressDownloading.onDownloading(((int) (bytesRead * 100 / expectedLength)));
                        }
                    }

                    @Override
                    protected void onDownloaded() {
                        if (progressCompleted != null) {
                            progressCompleted.onCompleted();
                        }
                    }

                    @Override
                    protected void onDelivered() {
                        if (progressPresent != null) {
                            progressPresent.onPresent();
                        }
                    }
                });
    }

    @Nullable
    protected DrawableRequestBuilder<Uri> getRequestBuilder(ImageView imageView) {

        DrawableRequestBuilder<Uri> request;
        try {
            if (this.fragment != null && this.fragment.get() != null) {
                request = getRequest(this.fragment.get());
            } else {
                Context context = getAvailableContext(imageView);
                if (context == null) {
                    return null;
                }
                request = getRequest(context);
            }
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
            String log = String.format("ImageLoader.getRequest Exception : %s", Log.getStackTraceString(e));
            Crashlytics.getInstance().core.log(log);
            return null;
        }

        if (backgroundColor != Integer.MAX_VALUE) {
            imageView.setBackgroundColor(backgroundColor);
        }


        request.fitCenter();

        request.diskCacheStrategy(DiskCacheStrategy.SOURCE);

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
        return request;
    }

    protected DrawableRequestBuilder<Uri> getRequestBuilder(SubsamplingScaleImageView imageView) {

        DrawableRequestBuilder<Uri> request;
        try {
            if (this.fragment != null && this.fragment.get() != null) {
                request = getRequest(this.fragment.get());
            } else {
                Context context = imageView.getContext();
                if (context == null) {
                    return null;
                }
                request = getRequest(context);
            }
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
            String log = String.format("ImageLoader.getRequest Exception : %s", Log.getStackTraceString(e));
            Crashlytics.getInstance().core.log(log);
            return null;
        }

        if (backgroundColor != Integer.MAX_VALUE) {
            imageView.setBackgroundColor(backgroundColor);
        }


        request.fitCenter();

        request.diskCacheStrategy(DiskCacheStrategy.SOURCE);

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
        return request;
    }

    @Nullable
    private DrawableTypeRequest<Uri> getRequest(Context context) throws Exception {
        if (blockNetworking) {
            return Glide.with(context)
                    // cache 되어 있는지 확인하기 위해 네트워킹 작업이 실행되면 exception 발생시킨다.
                    .using(new ThrowIOExceptionStreamLoader<Uri>())
                    .load(uri);
        } else {
            return Glide.with(context).load(uri);
        }
    }

    @Nullable
    private DrawableTypeRequest<Uri> getRequest(Fragment fragment) throws Exception {
        if (blockNetworking) {
            return Glide.with(fragment)
                    // cache 되어 있는지 확인하기 위해 네트워킹 작업이 실행되면 exception 발생시킨다.
                    .using(new ThrowIOExceptionStreamLoader<Uri>())
                    .load(uri);
        } else {
            return Glide.with(fragment).load(uri);
        }
    }

    @Nullable
    private Context getAvailableContext(ImageView imageView) {
        Context context = imageView.getContext();
        if (context == null) {
            return null;
        }

        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (isActivityDestroyed(activity) || activity.isFinishing()) {
                return null;
            }

            return context;
        } else {
            return context;
        }
    }

    private boolean isActivityDestroyed(Activity activity) {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed());
    }

    public interface ProgressStarted {
        void onStart();
    }

    public interface ProgressDownloading {
        void onDownloading(int progress);
    }

    public interface ProgressCompleted {
        void onCompleted();
    }

    public interface ProgressPresent {
        void onPresent();
    }
}
