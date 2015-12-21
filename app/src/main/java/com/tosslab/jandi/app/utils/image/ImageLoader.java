package com.tosslab.jandi.app.utils.image;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.GenericDraweeView;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.utils.UriFactory;

/**
 * Created by tonyjs on 15. 12. 21..
 */
public class ImageLoader {

    public static final int STANDARD_IMAGE_SIZE = 2048;

    private Builder builder;

    private ImageLoader(Builder builder) {
        this.builder = builder;
    }

    public ImageLoader load(GenericDraweeView draweeView) {
        draweeView.setAspectRatio(builder.getAspectRatio());

        setHierarchy(draweeView.getHierarchy());

        ImageRequest imageRequest = getImageRequest(draweeView.getLayoutParams());

        DraweeController controller = getController(imageRequest, draweeView.getController());

        draweeView.setController(controller);
        return this;
    }

    private void setHierarchy(GenericDraweeHierarchy hierarchy) {
        final Resources resources = JandiApplication.getContext().getResources();

        hierarchy.setActualImageScaleType(getScaleType(builder.getActualImageScaleType()));

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
            hierarchy.setPlaceholderImage(progressDrawable, scaleType);
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
            resizeWidth = layoutParams.width > 0 ? layoutParams.width : STANDARD_IMAGE_SIZE;
        }
        int resizeHeight = builder.getResizeHeight();
        if (resizeHeight <= 0) {
            final float aspectRatio = builder.getAspectRatio();
            if (aspectRatio > 0) {
                resizeHeight = (int) (resizeWidth * aspectRatio);
            } else {
                resizeHeight = layoutParams.height > 0 ? layoutParams.height : STANDARD_IMAGE_SIZE;
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
                .setControllerListener(builder.getListener())
                .build();
    }

    private ScalingUtils.ScaleType getScaleType(ScalingUtils.ScaleType scaleType) {
        return getScaleType(scaleType, ScalingUtils.ScaleType.FIT_XY);
    }

    private ScalingUtils.ScaleType getScaleType(ScalingUtils.ScaleType scaleType,
                                                ScalingUtils.ScaleType defaultScaleType) {
        return scaleType != null ? scaleType : defaultScaleType;
    }

    public static class Builder {
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

        private ControllerListener listener;

        private Uri uri;

        private int actualImageResourceId;
        private String path;
        private boolean fromFile;

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

        public Builder listener(ControllerListener controllerListener) {
            this.listener = controllerListener;
            return this;
        }

        public ImageLoader uri(Uri uri) {
            this.uri = uri;
            return new ImageLoader(this);
        }

        public ImageLoader path(String path, boolean fromFile) {
            this.path = path;
            this.fromFile = fromFile;
            return new ImageLoader(this);
        }

        public ImageLoader resource(int resId) {
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

        public ControllerListener getListener() {
            return listener;
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
}
