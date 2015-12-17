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
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.common.ImageDecodeOptionsBuilder;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.tosslab.jandi.app.JandiApplication;
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

    private final ImageView ivFileTypeIcon;
    private final ViewGroup vgDetailPhoto;
    private final SimpleDraweeView ivFilePhoto;
    private final ViewGroup vgTapToViewOriginal;
    private final View btnTapToViewOriginal;

    private final int roomId;
    private Context context;

    public ImageThumbLoader(ImageView ivFileTypeIcon, ViewGroup vgDetailPhoto, SimpleDraweeView ivFilePhoto,
                            ViewGroup vgTapToViewOriginal, int roomId) {
        this.ivFileTypeIcon = ivFileTypeIcon;
        this.vgDetailPhoto = vgDetailPhoto;
        this.ivFilePhoto = ivFilePhoto;
        this.roomId = roomId;
        this.vgTapToViewOriginal = vgTapToViewOriginal;

        btnTapToViewOriginal = vgTapToViewOriginal.findViewById(R.id.vg_file_detail_tap_to_view_description);

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
        hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);

        Drawable error = resources.getDrawable(R.drawable.file_messageview_noimage);
        hierarchy.setFailureImage(error, ScalingUtils.ScaleType.FIT_CENTER);

        ResMessages.ThumbnailUrls extraInfo = content.extraInfo;

        boolean hasThumbnailUrl = extraInfo != null && !TextUtils.isEmpty(extraInfo.largeThumbnailUrl);

        boolean hasSizeInfo = extraInfo != null && extraInfo.width > 0 && extraInfo.height > 0;

        ivFilePhoto.setOnClickListener(view -> moveToPhotoViewer(fileMessageId, content));

        if (!TextUtils.isEmpty(localFilePath)) {
            if (hasSizeInfo) {
                updateViewSize(extraInfo.width, extraInfo.height, extraInfo.orientation);
            }

            Drawable placeHolder = resources.getDrawable(R.drawable.file_messageview_downloading);
            hierarchy.setPlaceholderImage(placeHolder, ScalingUtils.ScaleType.FIT_CENTER);

            Uri uri = UriFactory.getFileUri(localFilePath);
            loadImage(uri, hierarchy, hasSizeInfo);

        } else {
            if (hasThumbnailUrl) {
                if (hasSizeInfo) {
                    updateViewSize(extraInfo.width, extraInfo.height, extraInfo.orientation);
                }
                Uri uri = Uri.parse(extraInfo.largeThumbnailUrl);

                Drawable placeHolder = resources.getDrawable(R.drawable.file_messageview_downloading);
                hierarchy.setPlaceholderImage(placeHolder, ScalingUtils.ScaleType.FIT_CENTER);

                loadImage(uri, hierarchy, hasSizeInfo);
                return;
            }

            Uri originalUri = Uri.parse(originalUrl);

            if (hasCache(originalUri)) {
                if (hasSizeInfo) {
                    updateViewSize(extraInfo.width, extraInfo.height, extraInfo.orientation);
                }
                Drawable placeHolder = resources.getDrawable(R.drawable.file_messageview_downloading);
                hierarchy.setPlaceholderImage(placeHolder, ScalingUtils.ScaleType.FIT_CENTER);
                loadImage(originalUri, hierarchy, hasSizeInfo);
                return;
            }

            vgTapToViewOriginal.setVisibility(View.VISIBLE);

            ivFilePhoto.setOnClickListener(null);

            vgTapToViewOriginal.setOnClickListener(v -> {
                vgTapToViewOriginal.setVisibility(View.GONE);
                CircleProgressDrawable progressDrawable = getCircleProgressDrawable(resources);
                hierarchy.setProgressBarImage(progressDrawable, ScalingUtils.ScaleType.CENTER);
                loadImage(originalUri, hierarchy, false);
                ivFilePhoto.setOnClickListener(view -> moveToPhotoViewer(fileMessageId, content));
            });
        }
    }

    private boolean hasCache(Uri originalUri) {
        DataSource<Boolean> dataSource = Fresco.getImagePipeline().isInDiskCache(originalUri);
        boolean isInDiskCache = dataSource.getResult() != null && dataSource.getResult();
        return isInDiskCache || Fresco.getImagePipeline().isInBitmapMemoryCache(originalUri);
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

        return progressDrawable;
    }

    private void loadImage(Uri uri, GenericDraweeHierarchy hierarchy,
                           boolean hasUpdateViewSizeBefore) {
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
        if (!hasUpdateViewSizeBefore) {
            draweeControllerBuilder.setControllerListener(new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo,
                                            Animatable animatable) {
                    LogUtil.i("tony", String.format("%s, %s", imageInfo.getWidth(), imageInfo.getHeight()));
                    updateViewSize(imageInfo.getWidth(), imageInfo.getHeight());
                }
            });
        }

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
        LogUtil.e("tony", String.format("%s, %s", imageWidth, imageHeight));

        int displayWidth = ApplicationUtil.getDisplaySize(false);
        int displayHeight = ApplicationUtil.getDisplaySize(true);

        ViewGroup.LayoutParams layoutParams = vgDetailPhoto.getLayoutParams();

        int viewWidth = vgDetailPhoto.getMeasuredWidth();
        if (imageWidth > imageHeight) {
            float ratio = imageHeight / (float) imageWidth;

            layoutParams.width = viewWidth;
            layoutParams.height = (int) (viewWidth * ratio);
        } else {
//            DisplayMetrics metrics = JandiApplication.getContext().getResources().getDisplayMetrics();
//            int photoWidth = (int) (Math.min(displayWidth, displayHeight) - (metrics.density * 22));
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
            String optimizedImageUrl = ImageUtil.getOptimizedImageUrl(content);
            PhotoViewActivity_
                    .intent(context)
                    .imageUrl(optimizedImageUrl)
                    .imageName(content.name)
                    .imageType(content.type)
                    .start();
        }
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewPhoto);
    }

}
