package com.tosslab.jandi.app.ui.filedetail.fileinfo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity_;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

/**
 * Created by Steve SeongUg Jung on 15. 4. 29..
 */
public class ImageThumbLoader implements FileThumbLoader {

    private final ImageView iconFileType;
    private final ImageView imageViewPhotoFile;
    private Context context;

    public ImageThumbLoader(ImageView iconFileType, ImageView imageViewPhotoFile) {
        this.iconFileType = iconFileType;
        this.imageViewPhotoFile = imageViewPhotoFile;
        context = imageViewPhotoFile.getContext();
    }

    @Override
    public void loadThumb(ResMessages.FileMessage fileMessage, int entityId) {
        ResMessages.FileContent content = fileMessage.content;
        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(content.serverUrl);
        iconFileType.setImageResource(
                MimeTypeUtil.getMimeTypeIconImage(content.serverUrl, content.icon));

        String thumbnailPhotoUrl =
                BitmapUtil.getThumbnailUrlOrOriginal(content, BitmapUtil.Thumbnails.LARGE);

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
                    loadImage(thumbnailPhotoUrl);
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
                    });
                    break;
                default:
                    imageViewPhotoFile.setOnClickListener(view -> {
                        String optimizedImageUrl = BitmapUtil.getOptimizedImageUrl(context, content);
                        CarouselViewerActivity_
                                .intent(context)
                                .startLinkId(fileMessage.id)
                                .start();
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
                    imageViewPhotoFile.setImageResource(R.drawable.jandi_down_img_disable);
                    break;
            }
        }
    }

    private void loadImage(String url) {
        if (url.toLowerCase().endsWith("gif")) {
            Ion.with(imageViewPhotoFile)
                    .fitCenter()
                    .placeholder(R.drawable.jandi_down_placeholder_img)
                    .error(R.drawable.jandi_down_img_disable)
                    .crossfade(true)
                    .load(url);
            return;
        }

        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.jandi_down_placeholder_img)
                .error(R.drawable.jandi_down_img_disable)
                .animate(view -> {
                    view.setAlpha(0.0f);
                    view.animate()
                            .alpha(1.0f)
                            .setDuration(300);
                })  // Avoid doesn't working 'fitCenter with crossfade'
                .fitCenter()
                .into(imageViewPhotoFile);
    }
}
