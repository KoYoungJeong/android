package com.tosslab.jandi.app.ui.filedetail.fileinfo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity_;
import com.tosslab.jandi.app.ui.photo.PhotoViewActivity_;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

/**
 * Created by Steve SeongUg Jung on 15. 4. 29..
 */
public class ImageThumbLoader implements FileThumbLoader {

    private final ImageView iconFileType;
    private final ViewGroup vgDetailPhoto;
    private final ImageView imageViewPhotoFile;
    private final int roomId;
    private Context context;

    public ImageThumbLoader(ImageView iconFileType, ViewGroup vgDetailPhoto, ImageView imageViewPhotoFile, int roomId) {
        this.iconFileType = iconFileType;
        this.vgDetailPhoto = vgDetailPhoto;
        this.imageViewPhotoFile = imageViewPhotoFile;
        this.roomId = roomId;
        context = imageViewPhotoFile.getContext();
    }

    @Override
    public void loadThumb(ResMessages.FileMessage fileMessage) {

        ResMessages.FileContent content = fileMessage.content;
        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(content.serverUrl);
        iconFileType.setImageResource(
                MimeTypeUtil.getMimeTypeIconImage(content.serverUrl, content.icon));


        if (BitmapUtil.hasImageUrl(content)) {
            imageViewPhotoFile.setEnabled(true);

            switch (sourceType) {
                case Google:
                    imageViewPhotoFile.setImageResource(R.drawable.jandi_down_placeholder_google);
                    break;
                case Dropbox:
                    imageViewPhotoFile.setImageResource(R.drawable.jandi_down_placeholder_dropbox);
                    break;
                default:
                    String localFilePath = BitmapUtil.getLocalFilePath(fileMessage.id);
                    String thumbnailPhotoUrl;
                    if (!TextUtils.isEmpty(localFilePath)) {
                        thumbnailPhotoUrl = localFilePath;
                    } else {
                        thumbnailPhotoUrl = BitmapUtil.getThumbnailUrlOrOriginal(content, BitmapUtil.Thumbnails.LARGE);
                    }

                    imageViewPhotoFile.setBackgroundResource(R.color.jandi_messages_image_bg);
                    BitmapUtil.loadImageByGlideOrIonWhenGif(
                            imageViewPhotoFile, thumbnailPhotoUrl,
                            R.drawable.file_messageview_downloading, R.drawable.file_messageview_noimage,
                            (width, height) -> {
                                updateViewSize(width, height);
                            });
                    break;
            }

            switch (sourceType) {
                case Google:
                case Dropbox:
                    imageViewPhotoFile.setOnClickListener(view -> {
                        String originalUrl =
                                BitmapUtil.getThumbnailUrlOrOriginal(
                                        content, BitmapUtil.Thumbnails.ORIGINAL);
                        context.startActivity(
                                new Intent(Intent.ACTION_VIEW, Uri.parse(originalUrl)));
                        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewPhoto);
                    });
                    break;
                default:
                    imageViewPhotoFile.setOnClickListener(view -> {

                        if (roomId > 0) {
                            CarouselViewerActivity_.intent(context)
                                    .roomId(roomId)
                                    .startLinkId(fileMessage.id)
                                    .start();
                        } else {
                            String optimizedImageUrl = BitmapUtil.getOptimizedImageUrl(content);
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
            imageViewPhotoFile.setEnabled(false);

            switch (sourceType) {
                case Google:
                    imageViewPhotoFile.setImageResource(R.drawable.jandi_down_placeholder_google);
                    break;
                case Dropbox:
                    imageViewPhotoFile.setImageResource(R.drawable.jandi_down_placeholder_dropbox);
                    break;
                default:
                    imageViewPhotoFile.setImageResource(R.drawable.file_down_img_disable);
                    break;
            }
        }
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
