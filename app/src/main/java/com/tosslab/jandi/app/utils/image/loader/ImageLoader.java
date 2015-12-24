package com.tosslab.jandi.app.utils.image.loader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.executors.UiThreadExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.GenericDraweeView;
import com.facebook.imagepipeline.animated.factory.AnimatedDrawableFactory;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableAnimatedImage;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.listener.ClosableAttachStateChangeListener;
import com.tosslab.jandi.app.utils.image.listener.OnResourceReadyCallback;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.concurrent.ExecutorService;

/**
 * Created by tonyjs on 15. 12. 21..
 */
public class ImageLoader {

    public static final String TAG = ImageLoader.class.getSimpleName();

    private Builder builder;

    ImageLoader(Builder builder) {
        this.builder = builder;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static void loadWithCallback(Uri uri, final OnResourceReadyCallback onResourceReadyCallback) {
        loadWithCallback(uri, null, false, onResourceReadyCallback);
    }

    public static void loadWithCallback(Uri uri,
                                        ResizeOptions resizeOptions,
                                        final OnResourceReadyCallback onResourceReadyCallback) {
        loadWithCallback(uri, resizeOptions, false, onResourceReadyCallback);
    }

    public static void loadWithCallback(Uri uri,
                                        ResizeOptions resizeOptions,
                                        boolean executeIntoCallerThread,
                                        final OnResourceReadyCallback onResourceReadyCallback) {
        if (resizeOptions == null) {
            final int maximumSize = ImageUtil.STANDARD_IMAGE_SIZE;
            resizeOptions = new ResizeOptions(maximumSize, maximumSize);
        }

        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri)
                .setAutoRotateEnabled(true)
                .setResizeOptions(resizeOptions)
                .build();

        loadWithPipeline(imageRequest, executeIntoCallerThread, onResourceReadyCallback);
    }

    private static void loadWithPipeline(ImageRequest imageRequest, boolean executeIntoCallerThread,
                                         OnResourceReadyCallback onResourceReadyCallback) {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource =
                imagePipeline.fetchDecodedImage(imageRequest, JandiApplication.getContext());

        ExecutorService executorService = executeIntoCallerThread
                ? CallerThreadExecutor.getInstance()
                : UiThreadExecutorService.getInstance();

        dataSource.subscribe(new BitmapDataSubscriber(onResourceReadyCallback), executorService);
    }

    public ImageLoader into(GenericDraweeView draweeView) {
        draweeView.setAspectRatio(builder.getAspectRatio());

        setHierarchy(draweeView.getHierarchy());

        ImageRequest imageRequest = getImageRequest(draweeView.getLayoutParams());

        DraweeController controller = getController(imageRequest, draweeView.getController());

        draweeView.setController(controller);

//        OnResourceReadyCallback callback = builder.getCallback();
//        if (callback == null) {
//            callback = new OnResourceReadyCallback() {
//                @Override
//                public void onReady(Drawable drawable, CloseableReference reference) {
//                    draweeView.getHierarchy().setImage(drawable, 1f, false);
//
//                    draweeView.addOnAttachStateChangeListener(
//                            new ClosableAttachStateChangeListener(reference));
//                }
//
//                @Override
//                public void onFail(Throwable cause) {
//                    LogUtil.e(TAG, Log.getStackTraceString(cause));
//
//                    draweeView.getHierarchy().setFailure(cause);
//                }
//
//                @Override
//                public void onProgressUpdate(float progress) {
//
//                }
//            };
//        }
//        loadWithPipeline(imageRequest, false, callback);
        return this;
    }

    private void setHierarchy(GenericDraweeHierarchy hierarchy) {
        final Resources resources = JandiApplication.getContext().getResources();

        ScalingUtils.ScaleType actualScaleType =
                getScaleType(builder.getActualImageScaleType(), ScalingUtils.ScaleType.CENTER_CROP);
        hierarchy.setActualImageScaleType(actualScaleType);

        final int placeHolder = builder.getPlaceHolder();
        final Drawable placeHolderDrawable = builder.getPlaceHolderDrawable();
        if (placeHolder <= 0 && placeHolderDrawable == null) {
            hierarchy.setPlaceholderImage(null);
        } else {
            ScalingUtils.ScaleType scaleType = getScaleType(builder.getPlaceHolderScaleType());
            Drawable drawable = placeHolderDrawable != null
                    ? placeHolderDrawable : resources.getDrawable(builder.getPlaceHolder());
            hierarchy.setPlaceholderImage(drawable, scaleType);
        }

        final int error = builder.getError();
        final Drawable errorDrawable = builder.getErrorDrawable();
        if (error <= 0 && errorDrawable == null) {
            hierarchy.setFailureImage(null);
        } else {
            ScalingUtils.ScaleType scaleType = getScaleType(builder.getErrorScaleType());
            Drawable drawable = errorDrawable != null
                    ? errorDrawable : resources.getDrawable(builder.getError());
            hierarchy.setFailureImage(drawable, scaleType);
        }

        final Drawable progressDrawable = builder.getProgressDrawable();
        if (progressDrawable != null) {
            ScalingUtils.ScaleType scaleType = getScaleType(
                    builder.getPlaceHolderScaleType(), ScalingUtils.ScaleType.CENTER);
            hierarchy.setProgressBarImage(progressDrawable, scaleType);
        } else {
            hierarchy.setProgressBarImage(null);
        }

        final RoundingParams roundingParams = builder.getRoundingParams();
        if (roundingParams != null) {
            hierarchy.setRoundingParams(roundingParams);
        } else {
            hierarchy.setRoundingParams(null);
        }
    }

    private ImageRequest getImageRequest(ViewGroup.LayoutParams layoutParams) {
        ImageRequestBuilder requestBuilder = null;
        final int actualImageResourceId = builder.getActualImageResourceId();
        if (actualImageResourceId > 0) {
            requestBuilder = ImageRequestBuilder.newBuilderWithResourceId(actualImageResourceId);
        } else {
            final Uri uri = builder.getUri();

            Uri requestUri = uri != null ? uri
                    : builder.isFromFile()
                    ? UriFactory.getFileUri(builder.getPath())
                    : Uri.parse(builder.getPath());

            requestBuilder = ImageRequestBuilder.newBuilderWithSource(requestUri);
        }

        requestBuilder.setAutoRotateEnabled(true);
        requestBuilder.setLocalThumbnailPreviewsEnabled(true);
        requestBuilder.setPostprocessor(builder.getProcessor());

        ImageDecodeOptions imageDecodeOptions = ImageDecodeOptions.newBuilder()
                .setDecodePreviewFrame(true)
                .setUseLastFrameForPreview(true)
                .setBackgroundColor(builder.getBackgroundColor() > 0
                        ? builder.getBackgroundColor() : Color.TRANSPARENT)
                .build();

        requestBuilder.setImageDecodeOptions(imageDecodeOptions);

        int resizeWidth = builder.getResizeWidth();
        if (resizeWidth <= 0) {
            resizeWidth = layoutParams.width > 0
                    ? layoutParams.width : ImageUtil.STANDARD_IMAGE_SIZE;
        }
        int resizeHeight = builder.getResizeHeight();
        if (resizeHeight <= 0) {
            final float aspectRatio = builder.getAspectRatio();
            if (aspectRatio > 0) {
                resizeHeight = (int) (resizeWidth * aspectRatio);
            } else {
                resizeHeight = layoutParams.height > 0
                        ? layoutParams.height : ImageUtil.STANDARD_IMAGE_SIZE;
            }
        }

        ResizeOptions resizeOptions = new ResizeOptions(resizeWidth, resizeHeight);
        requestBuilder.setResizeOptions(resizeOptions);

        return requestBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private DraweeController getController(ImageRequest imageRequest, DraweeController oldController) {
        return Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(true)
                .setImageRequest(imageRequest)
                .setOldController(oldController)
                .setControllerListener(builder.getControllerListener())
                .build();
    }

    private ScalingUtils.ScaleType getScaleType(ScalingUtils.ScaleType scaleType) {
        return getScaleType(scaleType, ScalingUtils.ScaleType.FIT_XY);
    }

    private ScalingUtils.ScaleType getScaleType(ScalingUtils.ScaleType scaleType,
                                                ScalingUtils.ScaleType defaultScaleType) {
        return scaleType != null ? scaleType : defaultScaleType;
    }

    public static final class Builder {
        private int placeHolder;
        private Drawable placeHolderDrawable;
        private ScalingUtils.ScaleType placeHolderScaleType;

        private Drawable progressDrawable;
        private ScalingUtils.ScaleType progressScaleType;

        private ScalingUtils.ScaleType actualImageScaleType;

        private int error;
        private Drawable errorDrawable;
        private ScalingUtils.ScaleType errorScaleType;

        private int backgroundColor;

        private RoundingParams roundingParams;

        private int resizeWidth;
        private int resizeHeight;
        private float aspectRatio;

        private Postprocessor processor;

        private OnResourceReadyCallback callback;
        private ControllerListener controllerListener;

        private Uri uri;

        private int actualImageResourceId;
        private String path;
        private boolean fromFile;

        Builder() {

        }

        public Builder placeHolder(int resId, ScalingUtils.ScaleType scaleType) {
            this.placeHolder = resId;
            this.placeHolderScaleType = scaleType;
            return this;
        }

        public Builder placeHolder(Drawable drawable, ScalingUtils.ScaleType scaleType) {
            this.placeHolderDrawable = drawable;
            this.placeHolderScaleType = scaleType;
            return this;
        }

        public Builder actualScaleType(ScalingUtils.ScaleType scaleType) {
            this.actualImageScaleType = scaleType;
            return this;
        }

        public Builder error(int resId, ScalingUtils.ScaleType scaleType) {
            this.error = resId;
            this.errorScaleType = scaleType;
            return this;
        }

        public Builder error(Drawable drawable, ScalingUtils.ScaleType scaleType) {
            this.errorDrawable = drawable;
            this.errorScaleType = scaleType;
            return this;
        }

        public Builder progress(Drawable drawable, ScalingUtils.ScaleType scaleType) {
            this.progressDrawable = drawable;
            this.progressScaleType = scaleType;
            return this;
        }

        public Builder aspectRatio(float ratio) {
            this.aspectRatio = ratio;
            return this;
        }

        public Builder backgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder roundingParams(RoundingParams roundingParams) {
            this.roundingParams = roundingParams;
            return this;
        }

        public Builder resize(int width, int height) {
            this.resizeWidth = width;
            this.resizeHeight = height;
            return this;
        }

        public Builder processor(Postprocessor postprocessor) {
            this.processor = postprocessor;
            return this;
        }

        public Builder callback(OnResourceReadyCallback callback) {
            this.callback = callback;
            return this;
        }

        public Builder controllerListener(ControllerListener listener) {
            this.controllerListener = listener;
            return this;
        }

        public ImageLoader load(Uri uri) {
            this.uri = uri;
            return new ImageLoader(this);
        }

        public ImageLoader load(String path, boolean fromFile) {
            this.path = path;
            this.fromFile = fromFile;
            return new ImageLoader(this);
        }

        public ImageLoader load(int resId) {
            this.actualImageResourceId = resId;
            return new ImageLoader(this);
        }

        public int getPlaceHolder() {
            return placeHolder;
        }

        public Drawable getPlaceHolderDrawable() {
            return placeHolderDrawable;
        }

        public ScalingUtils.ScaleType getPlaceHolderScaleType() {
            return placeHolderScaleType;
        }

        public Drawable getProgressDrawable() {
            return progressDrawable;
        }

        public ScalingUtils.ScaleType getProgressScaleType() {
            return progressScaleType;
        }

        public ScalingUtils.ScaleType getActualImageScaleType() {
            return actualImageScaleType;
        }

        public int getError() {
            return error;
        }

        public Drawable getErrorDrawable() {
            return errorDrawable;
        }

        public ScalingUtils.ScaleType getErrorScaleType() {
            return errorScaleType;
        }

        public float getAspectRatio() {
            return aspectRatio;
        }

        public int getBackgroundColor() {
            return backgroundColor;
        }

        public RoundingParams getRoundingParams() {
            return roundingParams;
        }

        public int getResizeWidth() {
            return resizeWidth;
        }

        public int getResizeHeight() {
            return resizeHeight;
        }

        public Postprocessor getProcessor() {
            return processor;
        }

        public OnResourceReadyCallback getCallback() {
            return callback;
        }

        public ControllerListener getControllerListener() {
            return controllerListener;
        }

        public Uri getUri() {
            return uri;
        }

        public int getActualImageResourceId() {
            return actualImageResourceId;
        }

        public String getPath() {
            return path;
        }

        public boolean isFromFile() {
            return fromFile;
        }

    }

    public static class BitmapDataSubscriber
            extends BaseDataSubscriber<CloseableReference<CloseableImage>> {

        private OnResourceReadyCallback onResourceReadyCallback;

        public BitmapDataSubscriber(OnResourceReadyCallback onResourceReadyCallback) {
            this.onResourceReadyCallback = onResourceReadyCallback;
        }

        @Override
        public void onNewResultImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
            if (!dataSource.isFinished() || onResourceReadyCallback == null) {
                return;
            }
            CloseableReference<CloseableImage> imageReference = dataSource.getResult();
            if (imageReference != null) {
                CloseableImage closeableImage = imageReference.get();
                Drawable drawable = getDrawable(closeableImage);
                if (drawable != null) {
                    onResourceReadyCallback.onReady(drawable, imageReference);
                } else {
                    onResourceReadyCallback.onFail(new NullPointerException("Drawable is empty."));
                    CloseableReference.closeSafely(imageReference);
                }
            } else {
                onResourceReadyCallback.onFail(new NullPointerException("ImageReference is empty."));
            }
        }

        private Drawable getDrawable(CloseableImage closeableImage) {
            if (closeableImage instanceof CloseableBitmap) {
                return getBitmapDrawable((CloseableBitmap) closeableImage);
            } else if (closeableImage instanceof CloseableAnimatedImage) {
                return getAnimatedDrawable((CloseableAnimatedImage) closeableImage);
            }
            return null;
        }

        private Drawable getAnimatedDrawable(CloseableAnimatedImage animatedImage) {
            LogUtil.i(TAG, "AnimatedImage loaded");
            AnimatedDrawableFactory animatedDrawableFactory =
                    Fresco.getImagePipelineFactory().getAnimatedDrawableFactory();
            return animatedDrawableFactory.create(animatedImage.getImageResult());
        }

        private Drawable getBitmapDrawable(CloseableBitmap closeableBitmap) {
            Bitmap underlyingBitmap = closeableBitmap.getUnderlyingBitmap();
            if (isValidateBitmap(underlyingBitmap)) {
                return new BitmapDrawable(
                        JandiApplication.getContext().getResources(), underlyingBitmap);
            }
            return null;
        }

        private boolean isValidateBitmap(Bitmap bitmap) {
            return bitmap != null && !bitmap.isRecycled();
        }

        @Override
        public void onFailureImpl(DataSource dataSource) {
            // handle failure
            if (onResourceReadyCallback != null) {
                onResourceReadyCallback.onFail(dataSource.getFailureCause());
            }
        }

        @Override
        public void onProgressUpdate(DataSource<CloseableReference<CloseableImage>> dataSource) {
            if (!dataSource.isFinished() && onResourceReadyCallback != null) {
                onResourceReadyCallback.onProgressUpdate(dataSource.getProgress());
            }
        }
    }
}
