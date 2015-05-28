package com.tosslab.jandi.app.ui.filedetail.fileinfo;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.photo.PhotoViewActivity_;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

/**
 * Created by Steve SeongUg Jung on 15. 4. 29..
 */
public class ImageThumbLoader implements FileThumbLoader {

    private final ImageView iconFileType;
    private final ImageView imageViewPhotoFile;

    public ImageThumbLoader(ImageView iconFileType, ImageView imageViewPhotoFile) {

        this.iconFileType = iconFileType;
        this.imageViewPhotoFile = imageViewPhotoFile;
    }

    @Override
    public void loadThumb(ResMessages.FileMessage fileMessage) {
        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(fileMessage.content.serverUrl);
        String photoUrl = BitmapUtil.getFileUrl(fileMessage.content.fileUrl);

        iconFileType.setImageResource(MimeTypeUtil.getMimeTypeIconImage(fileMessage.content.serverUrl, fileMessage.content.icon));

        String thumbnailPhotoUrl = null;
        if (fileMessage.content.extraInfo != null && !TextUtils.isEmpty(fileMessage.content.extraInfo.largeThumbnailUrl)) {

            thumbnailPhotoUrl = BitmapUtil.getFileUrl(fileMessage.content.extraInfo.largeThumbnailUrl);
        } else if (!TextUtils.isEmpty(fileMessage.content.fileUrl)) {
            thumbnailPhotoUrl = BitmapUtil.getFileUrl(fileMessage.content.fileUrl);
        }

        if (!TextUtils.isEmpty(thumbnailPhotoUrl) && !TextUtils.isEmpty(photoUrl)) {
            imageViewPhotoFile.setEnabled(true);

            switch (sourceType) {
                case Google:
                    imageViewPhotoFile.setImageResource(R.drawable.jandi_down_placeholder_google);
                    break;
                case Dropbox:
                    imageViewPhotoFile.setImageResource(R.drawable.jandi_down_placeholder_dropbox);
                    break;
                default:
                    Ion.with(imageViewPhotoFile)
                            .placeholder(R.drawable.jandi_down_placeholder_img)
                            .error(R.drawable.jandi_down_img_disable)
                            .fitCenter()
                            .crossfade(true)
                            .load(thumbnailPhotoUrl);
                    break;
            }


            final String finalPhotoUrl = photoUrl;
            switch (sourceType) {

                case Google:
                case Dropbox:
                    imageViewPhotoFile.setOnClickListener(view -> imageViewPhotoFile.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(finalPhotoUrl))));
                    break;
                default:
                    imageViewPhotoFile.setOnClickListener(view -> PhotoViewActivity_
                            .intent(imageViewPhotoFile.getContext())
                            .imageUrl(finalPhotoUrl)
                            .imageName(fileMessage.content.name)
                            .imageType(fileMessage.content.type)
                            .start());
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
}
