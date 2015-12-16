package com.tosslab.jandi.app.ui.filedetail.fileinfo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
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

        if (MimeTypeUtil.isFileFromGoogleOrDropbox(sourceType)) {
            int resourceId = sourceType == MimeTypeUtil.SourceType.Google
                    ? R.drawable.jandi_down_placeholder_google
                    : R.drawable.jandi_down_placeholder_dropbox;
            ivFilePhoto.setImageURI(UriFactory.getResourceUri(resourceId));

            if (hasImageUrl) {
                ivFilePhoto.setOnClickListener(view -> {
                    String originalUrl =
                            ImageUtil.getThumbnailUrlOrOriginal(content, ImageUtil.Thumbnails.ORIGINAL);
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

        if (!TextUtils.isEmpty(localFilePath)) {
            Drawable placeHolder = resources.getDrawable(R.drawable.file_messageview_downloading);
            hierarchy.setPlaceholderImage(placeHolder, ScalingUtils.ScaleType.FIT_CENTER);

            Uri uri = UriFactory.getFileUri(localFilePath);
            loadImage(uri, hierarchy, view -> moveToPhotoViewer(fileMessageId, content));
        } else {
            ResMessages.ThumbnailUrls extraInfo = content.extraInfo;
            final String originalUrl = content.fileUrl;
            if (extraInfo == null || TextUtils.isEmpty(extraInfo.largeThumbnailUrl)) {
                vgTapToViewOriginal.setVisibility(View.VISIBLE);
                vgTapToViewOriginal.setOnClickListener(v -> {
                    vgTapToViewOriginal.setVisibility(View.GONE);
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
                    hierarchy.setProgressBarImage(progressDrawable, ScalingUtils.ScaleType.CENTER);

                    loadImage(Uri.parse(originalUrl), hierarchy, v1 -> {
                        moveToPhotoViewer(fileMessageId, content);
                    });
                });
            } else {
                Uri uri = Uri.parse(extraInfo.largeThumbnailUrl);

                Drawable placeHolder = resources.getDrawable(R.drawable.file_messageview_downloading);
                hierarchy.setPlaceholderImage(placeHolder, ScalingUtils.ScaleType.FIT_CENTER);

                loadImage(uri, hierarchy, v -> {
                    moveToPhotoViewer(fileMessageId, content);
                });
            }
        }
//        if (hasImageUrl) {
//
//            switch (sourceType) {
//                case Google:
//                    ivFilePhoto.setImageURI(UriFactory.getResourceUri(R.drawable.jandi_down_placeholder_google));
//                    break;
//                case Dropbox:
//                    ivFilePhoto.setImageURI(UriFactory.getResourceUri(R.drawable.jandi_down_placeholder_dropbox));
//                    break;
//                default:
//                    loadImage(fileMessage.id, content);
//                    break;
//            }
//
//            switch (sourceType) {
//                case Google:
//                case Dropbox:
//                    ivFilePhoto.setOnClickListener(view -> {
//                        String originalUrl =
//                                ImageUtil.getThumbnailUrlOrOriginal(
//                                        content, ImageUtil.Thumbnails.ORIGINAL);
//                        context.startActivity(
//                                new Intent(Intent.ACTION_VIEW, Uri.parse(originalUrl)));
//                        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewPhoto);
//                    });
//                    break;
//                default:
//                    ivFilePhoto.setOnClickListener(view -> {
//                        if (roomId > 0) {
//                            CarouselViewerActivity_.intent(context)
//                                    .roomId(roomId)
//                                    .startLinkId(fileMessage.id)
//                                    .start();
//                        } else {
//                            String optimizedImageUrl = ImageUtil.getOptimizedImageUrl(content);
//                            PhotoViewActivity_
//                                    .intent(context)
//                                    .imageUrl(optimizedImageUrl)
//                                    .imageName(content.name)
//                                    .imageType(content.type)
//                                    .start();
//                        }
//                        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewPhoto);
//                    });
//                    break;
//            }
//
//        } else {
//            ivFilePhoto.setEnabled(false);
//
//            switch (sourceType) {
//                case Google:
//                    ivFilePhoto.setImageURI(UriFactory.getResourceUri(R.drawable.jandi_down_placeholder_google));
//                    break;
//                case Dropbox:
//                    ivFilePhoto.setImageURI(UriFactory.getResourceUri(R.drawable.jandi_down_placeholder_dropbox));
//                    break;
//                default:
//                    ivFilePhoto.setImageURI(UriFactory.getResourceUri(R.drawable.file_down_img_disable));
//                    break;
//            }
//        }
    }

    private void loadImage(Uri uri,
                           GenericDraweeHierarchy hierarchy,
                           View.OnClickListener onClickListener) {
        ivFilePhoto.setHierarchy(hierarchy);

        int displayWidth = ApplicationUtil.getDisplaySize(false);
        int displayHeight = ApplicationUtil.getDisplaySize(true);
        ResizeOptions resizeOptions = new ResizeOptions(displayWidth, displayHeight);
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(resizeOptions)
                .setAutoRotateEnabled(true)
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(imageRequest)
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo,
                                                Animatable animatable) {
                        updateViewSize(imageInfo.getWidth(), imageInfo.getHeight());
                    }
                })
                .setAutoPlayAnimations(true)
                .build();
        ivFilePhoto.setController(controller);

        ivFilePhoto.setOnClickListener(onClickListener);
    }

    private void updateViewSize(int imageWidth, int imageHeight) {
        int displayWidth = ApplicationUtil.getDisplaySize(false);
        int displayHeight = ApplicationUtil.getDisplaySize(true);

        ViewGroup.LayoutParams layoutParams = vgDetailPhoto.getLayoutParams();
        if (imageWidth <= 0 || imageHeight <= 0) {
            imageWidth = displayWidth;
            imageHeight = displayWidth;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        if (imageWidth > imageHeight) {
            int viewWidth = vgDetailPhoto.getMeasuredWidth();
            float ratio = (viewWidth * 10f) / (imageWidth * 10f);

            layoutParams.width = (int) (imageWidth * ratio);
            layoutParams.height = (int) (imageHeight * ratio);
        } else {
            DisplayMetrics metrics = JandiApplication.getContext().getResources().getDisplayMetrics();
            int photoWidth = (int) (Math.min(displayWidth, displayHeight) - (metrics.density * 22));
            layoutParams.width = photoWidth;
            layoutParams.height = photoWidth;
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
