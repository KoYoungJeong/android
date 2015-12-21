package com.tosslab.jandi.app.ui.filedetail.fileinfo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.common.references.CloseableReference;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity_;
import com.tosslab.jandi.app.ui.photo.PhotoViewActivity_;
import com.tosslab.jandi.app.ui.photo.widget.CircleProgressBar;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.image.BaseOnResourceReadyCallback;
import com.tosslab.jandi.app.utils.image.ClosableAttachStateChangeListener;
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
    private final ViewGroup vgProgressBar;
    private final CircleProgressBar progressBar;
    private final TextView tvPercentage;

    private final int roomId;
    private Context context;

    public ImageThumbLoader(ImageView ivFileTypeIcon, ViewGroup vgDetailPhoto, SimpleDraweeView ivFilePhoto,
                            ViewGroup vgTapToViewOriginal, int roomId) {
        this.ivFileTypeIcon = ivFileTypeIcon;
        this.vgDetailPhoto = vgDetailPhoto;
        this.ivFilePhoto = ivFilePhoto;
        this.roomId = roomId;

        this.vgTapToViewOriginal = vgTapToViewOriginal;
        this.vgProgressBar = (ViewGroup) vgDetailPhoto.findViewById(R.id.vg_file_detail_progress);
        this.progressBar = (CircleProgressBar) vgDetailPhoto.findViewById(R.id.progress_photoview);
        this.tvPercentage = (TextView) vgDetailPhoto.findViewById(R.id.tv_photoview_percentage);

        context = ivFilePhoto.getContext();

        setupProgress();
    }

    private void setupProgress() {
        Resources resources = context.getResources();
        int progressWidth = (int) (resources.getDisplayMetrics().density * 3);
        progressBar.setBgStrokeWidth(progressWidth);
        progressBar.setProgressStrokeWidth(progressWidth);
        progressBar.setBgColor(resources.getColor(R.color.white));
        progressBar.setProgressColor(resources.getColor(R.color.jandi_accent_color));
        progressBar.setMax(100);
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
                    AnalyticsUtil.sendEvent(
                            AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewPhoto);
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

        final GenericDraweeHierarchy hierarchy = ivFilePhoto.getHierarchy();
        hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);

        Drawable error = resources.getDrawable(R.drawable.file_messageview_noimage);
        hierarchy.setFailureImage(error, ScalingUtils.ScaleType.FIT_CENTER);

        ResMessages.ThumbnailUrls extraInfo = content.extraInfo;

        boolean hasThumbnailUrl = extraInfo != null && !TextUtils.isEmpty(extraInfo.largeThumbnailUrl);

        ivFilePhoto.setOnClickListener(view -> moveToPhotoViewer(fileMessageId, content));

        Uri originalUri = Uri.parse(ImageUtil.getImageFileUrl(originalUrl));
        if (TextUtils.isEmpty(localFilePath)
                && !hasThumbnailUrl
                && !ImageUtil.hasCache(originalUri)) {

            showTapToViewLayout(hierarchy, originalUri, fileMessageId, content);

        } else {
            Drawable placeHolder = resources.getDrawable(R.drawable.file_messageview_downloading);
            hierarchy.setPlaceholderImage(placeHolder, ScalingUtils.ScaleType.FIT_CENTER);
            ivFilePhoto.setHierarchy(hierarchy);

            Uri uri = !TextUtils.isEmpty(localFilePath)
                    ? UriFactory.getFileUri(localFilePath)
                    : hasThumbnailUrl
                    ? Uri.parse(extraInfo.largeThumbnailUrl) : originalUri;

            ResizeOptions resizeOptions = null;
            boolean hasSizeInfo = extraInfo != null && extraInfo.width > 0 && extraInfo.height > 0;
            if (hasSizeInfo) {
                Pair<Integer, Integer> widthAndHeight
                        = updateViewSize(extraInfo.width, extraInfo.height, extraInfo.orientation);
                int width = widthAndHeight.first;
                int height = widthAndHeight.second;
                if (width <= 0 || height <= 0) {
                    resizeOptions = getDefaultResizeOptions();
                } else {
                    resizeOptions = new ResizeOptions(width, height);
                }
            } else {
                resizeOptions = getDefaultResizeOptions();
            }

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setResizeOptions(resizeOptions)
                    .setAutoRotateEnabled(true)
                    .build();

            loadImage(request, hasSizeInfo);
        }
    }

    private void loadImage(ImageRequest imageRequest,
                           final boolean hasUpdateViewSizeBefore) {

        PipelineDraweeControllerBuilder draweeControllerBuilder = Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(true)
                .setImageRequest(imageRequest);
        if (!hasUpdateViewSizeBefore) {
            draweeControllerBuilder.setControllerListener(new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo,
                                            Animatable animatable) {
                    updateViewSize(imageInfo.getWidth(), imageInfo.getHeight());
                }
            });
        }

        ivFilePhoto.setController(draweeControllerBuilder.build());
    }

    private void showTapToViewLayout(final GenericDraweeHierarchy hierarchy, final Uri originalUri,
                                     int fileMessageId, ResMessages.FileContent content) {

        vgTapToViewOriginal.setVisibility(View.VISIBLE);

        ivFilePhoto.setOnClickListener(null);

        ivFilePhoto.setHierarchy(hierarchy);

        vgTapToViewOriginal.setOnClickListener(v -> {
            vgTapToViewOriginal.setVisibility(View.GONE);
            vgProgressBar.setVisibility(View.VISIBLE);

            loadImageWithCallback(originalUri, hierarchy);

            ivFilePhoto.setOnClickListener(view -> moveToPhotoViewer(fileMessageId, content));
        });
    }

    private void loadImageWithCallback(Uri uri, final GenericDraweeHierarchy hierarchy) {
        ImageUtil.loadDrawable(uri, new BaseOnResourceReadyCallback() {
            @Override
            public void onReady(Drawable drawable, CloseableReference reference) {
                progressBar.setProgress(100);
                tvPercentage.setText(100 + "%");
                vgProgressBar.setVisibility(View.GONE);

                updateViewSize(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                hierarchy.setImage(drawable, 1f, false);

                ivFilePhoto.addOnAttachStateChangeListener(
                        new ClosableAttachStateChangeListener(reference));
            }

            @Override
            public void onProgressUpdate(float progress) {
                int percentage = (int) (progress * 100);
                LogUtil.i(TAG, "onProgressUpdate = " + percentage);
                percentage = Math.min(percentage, 99);

                progressBar.setProgress(percentage);
                tvPercentage.setText(percentage + "%");
            }
        });
    }

    private ResizeOptions getDefaultResizeOptions() {
        return new ResizeOptions(ImageUtil.STANDARD_IMAGE_SIZE, ImageUtil.STANDARD_IMAGE_SIZE);
    }

    private Pair<Integer, Integer> updateViewSize(int imageWidth, int imageHeight, int orientation) {
        if (imageWidth <= 0 || imageHeight <= 0) {
            return new Pair<>(0, 0);
        }

        if (ImageUtil.isVerticalPhoto(orientation)) {
            int temp = imageHeight;
            imageHeight = imageWidth;
            imageWidth = temp;
        }

        return updateViewSize(imageWidth, imageHeight);
    }

    private Pair<Integer, Integer> updateViewSize(int imageWidth, int imageHeight) {
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
        return new Pair<>(layoutParams.width, layoutParams.height);
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
