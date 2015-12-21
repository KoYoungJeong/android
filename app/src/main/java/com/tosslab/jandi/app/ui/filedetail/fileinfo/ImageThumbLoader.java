package com.tosslab.jandi.app.ui.filedetail.fileinfo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.drawables.CircleProgressDrawable;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity_;
import com.tosslab.jandi.app.ui.photo.PhotoViewActivity_;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

/**
 * Created by Steve SeongUg Jung on 15. 4. 29..
 */
public class ImageThumbLoader implements FileThumbLoader {

    public static final String TAG = ImageThumbLoader.class.getSimpleName();
    private final ImageView ivFileTypeIcon;
    private final ViewGroup vgDetailPhoto;
    private final SimpleDraweeView ivFilePhoto;
    private final ViewGroup vgTapToViewOriginal;

    private final int roomId;
    private Context context;

    public ImageThumbLoader(ImageView ivFileTypeIcon, ViewGroup vgDetailPhoto, SimpleDraweeView ivFilePhoto,
                            ViewGroup vgTapToViewOriginal, int roomId) {
        this.ivFileTypeIcon = ivFileTypeIcon;
        this.vgDetailPhoto = vgDetailPhoto;
        this.ivFilePhoto = ivFilePhoto;
        this.roomId = roomId;
        this.vgTapToViewOriginal = vgTapToViewOriginal;

        context = ivFilePhoto.getContext();
    }

    @Override
    public void loadThumb(ResMessages.FileMessage fileMessage) {
        final ResMessages.FileContent content = fileMessage.content;

        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(content.serverUrl);
        int mimeTypeIconImage = MimeTypeUtil.getMimeTypeIconImage(content.serverUrl, content.icon);
        ivFileTypeIcon.setImageResource(mimeTypeIconImage);

        boolean hasImageUrl = ImageUtil.hasImageUrl(content);
        ivFilePhoto.setEnabled(hasImageUrl);

        final String originalUrl =
                ImageUtil.getThumbnailUrlOrOriginal(content, ImageUtil.Thumbnails.ORIGINAL);

        if (MimeTypeUtil.isFileFromGoogleOrDropbox(sourceType)) {
            int resourceId = sourceType == MimeTypeUtil.SourceType.Google
                    ? R.drawable.jandi_down_placeholder_google
                    : R.drawable.jandi_down_placeholder_dropbox;
            ivFilePhoto.setImageURI(UriFactory.getResourceUri(resourceId));

            if (hasImageUrl) {
                ivFilePhoto.setOnClickListener(view -> {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(originalUrl)));
                    AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewPhoto);
                });
            } else {
                ivFilePhoto.setOnClickListener(null);
            }
            return;
        }

        if (!hasImageUrl) {
            ivFilePhoto.setImageURI(UriFactory.getResourceUri(R.drawable.file_down_img_disable));
            ivFilePhoto.setOnClickListener(null);
            return;
        }

        final int fileMessageId = fileMessage.id;

        String localFilePath = ImageUtil.getLocalFilePath(fileMessage.id);
        Resources resources = context.getResources();

        GenericDraweeHierarchy hierarchy = ivFilePhoto.getHierarchy();

        Drawable placeHolder = resources.getDrawable(R.drawable.file_messageview_downloading);
        hierarchy.setPlaceholderImage(placeHolder, ScalingUtils.ScaleType.FIT_CENTER);

        hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);

        Drawable error = resources.getDrawable(R.drawable.file_messageview_noimage);
        hierarchy.setFailureImage(error, ScalingUtils.ScaleType.FIT_CENTER);

        ResMessages.ThumbnailUrls extraInfo = content.extraInfo;

        boolean hasThumbnailUrl = extraInfo != null && !TextUtils.isEmpty(extraInfo.largeThumbnailUrl);

        boolean hasSizeInfo = extraInfo != null && extraInfo.width > 0 && extraInfo.height > 0;

        ivFilePhoto.setOnClickListener(view -> moveToPhotoViewer(fileMessageId, content));

        Uri originalUri = Uri.parse(ImageUtil.getImageFileUrl(originalUrl));
        if (TextUtils.isEmpty(localFilePath)
                && !hasThumbnailUrl
                && !ImageUtil.hasCache(originalUri)) {
            vgTapToViewOriginal.setVisibility(View.VISIBLE);

            ivFilePhoto.setOnClickListener(null);

            vgTapToViewOriginal.setOnClickListener(v -> {
                vgTapToViewOriginal.setVisibility(View.GONE);
                hierarchy.setPlaceholderImage(null);
                CircleProgressDrawable progressDrawable = getCircleProgressDrawable(resources);
                hierarchy.setProgressBarImage(progressDrawable, ScalingUtils.ScaleType.CENTER);
                loadImage(originalUri, hierarchy, false);
                ivFilePhoto.setOnClickListener(view -> moveToPhotoViewer(fileMessageId, content));
            });
        } else {
            Uri uri = !TextUtils.isEmpty(localFilePath)
                    ? UriFactory.getFileUri(localFilePath)
                    : hasThumbnailUrl
                    ? Uri.parse(extraInfo.largeThumbnailUrl) : originalUri;

            if (hasSizeInfo) {
                updateViewSize(extraInfo.width, extraInfo.height, extraInfo.orientation);
            }

            loadImage(uri, hierarchy, hasSizeInfo);
        }
    }

    @NonNull
    private CircleProgressDrawable getCircleProgressDrawable(Resources resources) {
        CircleProgressDrawable progressDrawable = new CircleProgressDrawable(context);

        float density = resources.getDisplayMetrics().density;
        int progressWidth = (int) (density * 3);
        progressDrawable.setBackgroundColor(resources.getColor(R.color.jandi_file_detail_tab_to_view_bg));
        progressDrawable.setBgStrokeWidth(progressWidth);
        progressDrawable.setProgressStrokeWidth(progressWidth);
        progressDrawable.setBgProgressColor(resources.getColor(R.color.white));
        progressDrawable.setProgressColor(resources.getColor(R.color.jandi_accent_color));
        progressDrawable.setTextColor(Color.WHITE);

        float scaledDensity = resources.getDisplayMetrics().scaledDensity;
        progressDrawable.setTextSize((int) (scaledDensity * 14));
        progressDrawable.setIndicatorTextColor(resources.getColor(R.color.jandi_text_light));
        progressDrawable.setIndicatorTextSize((int) (scaledDensity * 14));
        progressDrawable.setIndicatorTextMargin((int) (density * 10));
        progressDrawable.setProgressWidth((int) (density * 58));

        progressDrawable.setTopMargin((int) (density * 21));

        return progressDrawable;
    }

    private void loadImage(Uri uri, GenericDraweeHierarchy hierarchy,
                           final boolean hasUpdateViewSizeBefore) {
        ivFilePhoto.setHierarchy(hierarchy);

        int displayWidth = ApplicationUtil.getDisplaySize(false);
        int displayHeight = ApplicationUtil.getDisplaySize(true);
        ResizeOptions resizeOptions = new ResizeOptions(displayWidth, displayHeight);

        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(resizeOptions)
                .setAutoRotateEnabled(true)
                .build();
        PipelineDraweeControllerBuilder draweeControllerBuilder = Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(true)
                .setImageRequest(imageRequest);
        draweeControllerBuilder.setControllerListener(new BaseControllerListener<ImageInfo>() {

            @Override
            public void onSubmit(String id, Object callerContext) {
                LogUtil.d(TAG, "onSubmit");
            }

            @Override
            public void onRelease(String id) {
                LogUtil.i(TAG, "onRelease");
            }

            @Override
            public void onIntermediateImageFailed(String id, Throwable throwable) {
                LogUtil.e(TAG, "onIntermediateImageFailed");
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                LogUtil.e(TAG, "onFailure");
            }

            @Override
            public void onIntermediateImageSet(String id, ImageInfo imageInfo) {
                LogUtil.d(TAG, "onIntermediateImageSet");
            }

            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo,
                                        Animatable animatable) {
                LogUtil.i(TAG, "onFinalImageSet");
                if (!hasUpdateViewSizeBefore) {
                    updateViewSize(imageInfo.getWidth(), imageInfo.getHeight());
                }
            }
        });

        ivFilePhoto.setController(draweeControllerBuilder.build());
    }

    private void updateViewSize(int imageWidth, int imageHeight, int orientation) {
        if (imageWidth <= 0 || imageHeight <= 0) {
            return;
        }

        if (ImageUtil.isVerticalPhoto(orientation)) {
            int temp = imageHeight;
            imageHeight = imageWidth;
            imageWidth = temp;
        }

        updateViewSize(imageWidth, imageHeight);
    }

    private void updateViewSize(int imageWidth, int imageHeight) {
        ViewGroup.LayoutParams layoutParams = vgDetailPhoto.getLayoutParams();

        int viewWidth = vgDetailPhoto.getMeasuredWidth();
        if (imageWidth > imageHeight) {
            float ratio = imageHeight / (float) imageWidth;

            layoutParams.width = viewWidth;
            layoutParams.height = (int) (viewWidth * ratio);
        } else {
            layoutParams.height = viewWidth;
        }

        vgDetailPhoto.setLayoutParams(layoutParams);
    }

    private void moveToPhotoViewer(int fileMessageId, ResMessages.FileContent content) {
        if (roomId > 0) {
            CarouselViewerActivity_.intent(context)
                    .roomId(roomId)
                    .startLinkId(fileMessageId)
                    .start();
        } else {
            String thumbUrl = ImageUtil.getThumbnailUrl(content.extraInfo, ImageUtil.Thumbnails.THUMB);
            PhotoViewActivity_
                    .intent(context)
                    .thumbUrl(thumbUrl)
                    .originalUrl(content.fileUrl)
                    .imageName(content.name)
                    .imageType(content.type)
                    .start();
        }
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewPhoto);
    }

}
