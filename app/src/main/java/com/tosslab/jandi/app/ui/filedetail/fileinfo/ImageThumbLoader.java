package com.tosslab.jandi.app.ui.filedetail.fileinfo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiApplication;
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


                    loadImageByGlideOrIonWhenGif(
                            imageViewPhotoFile, thumbnailPhotoUrl,
                            R.drawable.jandi_down_placeholder_img, R.drawable.jandi_down_placeholder_img,
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
                        String optimizedImageUrl = BitmapUtil.getOptimizedImageUrl(content);

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

    private void loadImageByGlideOrIonWhenGif(ImageView imageView,
                                              String url, int placeHolder, int error, OnResourceReady onResourceReady) {
        if (url.toLowerCase().endsWith("gif")) {
            Ion.with(imageView)
                    .fitCenter()
                    .placeholder(placeHolder)
                    .error(error)
                    .crossfade(true)
                    .load(url).withBitmapInfo().setCallback((e, result) -> {
                if (e == null && onResourceReady != null) {
                    Bitmap bitmap = result.getBitmapInfo().bitmap;
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    onResourceReady.onReady(width, height);
                }
            });
            return;
        }

        Glide.with(JandiApplication.getContext())
                .load(url)
                .placeholder(placeHolder)
                .error(error)
                .animate(view -> {
                    view.setAlpha(0.0f);
                    view.animate()
                            .alpha(1.0f)
                            .setDuration(300);
                })  // Avoid doesn't working 'fitCenter with crossfade'
                .fitCenter()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (onResourceReady != null) {
                            onResourceReady.onReady(resource.getIntrinsicWidth(), resource.getIntrinsicHeight());
                        }
                        return false;
                    }
                })
                .into(imageView);
    }

    private interface OnResourceReady {
        void onReady(int width, int height);
    }
}
