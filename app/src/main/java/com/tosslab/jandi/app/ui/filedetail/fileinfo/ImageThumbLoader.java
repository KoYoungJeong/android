package com.tosslab.jandi.app.ui.filedetail.fileinfo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity_;
import com.tosslab.jandi.app.ui.photo.PhotoViewActivity_;
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

    private final ImageView ivFileType;
    private final ViewGroup vgDetailPhoto;
    private final SimpleDraweeView ivFile;
    private final int roomId;
    private Context context;

    public ImageThumbLoader(ImageView ivFileType, ViewGroup vgDetailPhoto, SimpleDraweeView ivFile, int roomId) {
        this.ivFileType = ivFileType;
        this.vgDetailPhoto = vgDetailPhoto;
        this.ivFile = ivFile;
        this.roomId = roomId;
        context = ivFile.getContext();
    }

    @Override
    public void loadThumb(ResMessages.FileMessage fileMessage) {

        ResMessages.FileContent content = fileMessage.content;
        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(content.serverUrl);
        ivFileType.setImageResource(
                MimeTypeUtil.getMimeTypeIconImage(content.serverUrl, content.icon));

        if (ImageUtil.hasImageUrl(content)) {
            ivFile.setEnabled(true);

            switch (sourceType) {
                case Google:
                    ivFile.setImageURI(UriFactory.getResourceUri(R.drawable.jandi_down_placeholder_google));
                    break;
                case Dropbox:
                    ivFile.setImageURI(UriFactory.getResourceUri(R.drawable.jandi_down_placeholder_dropbox));
                    break;
                default:
                    loadImage(fileMessage.id, content);
                    break;
            }

            switch (sourceType) {
                case Google:
                case Dropbox:
                    ivFile.setOnClickListener(view -> {
                        String originalUrl =
                                ImageUtil.getThumbnailUrlOrOriginal(
                                        content, ImageUtil.Thumbnails.ORIGINAL);
                        context.startActivity(
                                new Intent(Intent.ACTION_VIEW, Uri.parse(originalUrl)));
                        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewPhoto);
                    });
                    break;
                default:
                    ivFile.setOnClickListener(view -> {
                        String optimizedImageUrl = ImageUtil.getOptimizedImageUrl(content);

                        if (roomId > 0) {
                            CarouselViewerActivity_.intent(context)
                                    .roomId(roomId)
                                    .startLinkId(fileMessage.id)
                                    .start();
                        } else {
                            PhotoViewActivity_
                                    .intent(context)
                                    .imageUrl(optimizedImageUrl)
                                    .imageName(content.name)
                                    .imageType(content.type)
                                    .start();
                        }
                        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewPhoto);
                    });
                    break;
            }

        } else {
            ivFile.setEnabled(false);

            switch (sourceType) {
                case Google:
                    ivFile.setImageURI(UriFactory.getResourceUri(R.drawable.jandi_down_placeholder_google));
                    break;
                case Dropbox:
                    ivFile.setImageURI(UriFactory.getResourceUri(R.drawable.jandi_down_placeholder_dropbox));
                    break;
                default:
                    ivFile.setImageURI(UriFactory.getResourceUri(R.drawable.file_down_img_disable));
                    break;
            }
        }
    }

    private void loadImage(int fileId, ResMessages.FileContent content) {
        String localFilePath = ImageUtil.getLocalFilePath(fileId);

        boolean isFromLocalFilePath = !TextUtils.isEmpty(localFilePath);

        String thumbnailPhotoUrl = isFromLocalFilePath
                ? localFilePath
                : ImageUtil.getThumbnailUrlOrOriginal(content, ImageUtil.Thumbnails.LARGE);

        Resources resources = context.getResources();

        GenericDraweeHierarchy hierarchy = ivFile.getHierarchy();
        hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);

        Drawable placeHolder = resources.getDrawable(R.drawable.file_messageview_downloading);
        hierarchy.setPlaceholderImage(placeHolder, ScalingUtils.ScaleType.FIT_CENTER);

        Drawable error = resources.getDrawable(R.drawable.file_messageview_noimage);
        hierarchy.setFailureImage(error, ScalingUtils.ScaleType.FIT_CENTER);

        ivFile.setHierarchy(hierarchy);

        Uri uri = isFromLocalFilePath
                ? UriFactory.getFileUri(thumbnailPhotoUrl)
                : Uri.parse(thumbnailPhotoUrl);

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo,
                                                Animatable animatable) {
                        updateViewSize(imageInfo.getWidth(), imageInfo.getHeight());
                    }
                })
                .setAutoPlayAnimations(true)
                .build();
        ivFile.setController(controller);
    }

    private void updateViewSize(int imageWidth, int imageHeight) {
        ViewGroup.LayoutParams layoutParams = vgDetailPhoto.getLayoutParams();
        if (imageWidth > imageHeight) {

            int viewWidth = vgDetailPhoto.getMeasuredWidth();
            float ratio = (viewWidth * 10f) / (imageWidth * 10f);

            layoutParams.width = (int) (imageWidth * ratio);
            layoutParams.height = (int) (imageHeight * ratio);
        } else {
            int photoWidth = vgDetailPhoto.getMeasuredWidth();
            layoutParams.width = photoWidth;
            layoutParams.height = photoWidth;

        }
        vgDetailPhoto.setLayoutParams(layoutParams);
    }

}
