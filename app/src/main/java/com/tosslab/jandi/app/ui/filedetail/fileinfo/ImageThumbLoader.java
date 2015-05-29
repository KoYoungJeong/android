package com.tosslab.jandi.app.ui.filedetail.fileinfo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.DrawableCrossFadeFactory;
import com.bumptech.glide.request.animation.ViewPropertyAnimation;
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
    private Context context;

    public ImageThumbLoader(ImageView iconFileType, ImageView imageViewPhotoFile) {
        this.iconFileType = iconFileType;
        this.imageViewPhotoFile = imageViewPhotoFile;
        context = imageViewPhotoFile.getContext();
    }

    @Override
    public void loadThumb(ResMessages.FileMessage fileMessage) {
        ResMessages.FileContent content = fileMessage.content;
        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(content.serverUrl);
        iconFileType.setImageResource(
                MimeTypeUtil.getMimeTypeIconImage(content.serverUrl, content.icon));

        ResMessages.ThumbnailUrls extraInfo = content.extraInfo;

        String smallThumbnailUrl = extraInfo != null ? extraInfo.smallThumbnailUrl : null;
        String mediumThumbnailUrl = extraInfo != null ? extraInfo.mediumThumbnailUrl : null;
        String largeThumbnailUrl = extraInfo != null ? extraInfo.largeThumbnailUrl : null;
        String originalFileUrl = content.fileUrl;

        String thumbnailPhotoUrl = !TextUtils.isEmpty(largeThumbnailUrl)
                ? BitmapUtil.getFileUrl(largeThumbnailUrl) : BitmapUtil.getFileUrl(originalFileUrl);

        if (hasImageUrl(
                smallThumbnailUrl, mediumThumbnailUrl, largeThumbnailUrl, originalFileUrl)) {
            imageViewPhotoFile.setEnabled(true);

            switch (sourceType) {
                case Google:
                    imageViewPhotoFile.setImageResource(R.drawable.jandi_down_placeholder_google);
                    break;
                case Dropbox:
                    imageViewPhotoFile.setImageResource(R.drawable.jandi_down_placeholder_dropbox);
                    break;
                default:
                    Glide.with(context)
                            .load(thumbnailPhotoUrl)
                            .placeholder(R.drawable.jandi_down_placeholder_img)
                            .error(R.drawable.jandi_down_img_disable)
                            .animate(view -> {
                                AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
                                anim.setDuration(300);
                                view.setAnimation(anim);
                            })  // Avoid doesn't working 'fitCenter'
                            .fitCenter()
                            .into(imageViewPhotoFile);

                    // 계단현상...
//                    Ion.with(imageViewPhotoFile)
//                            .placeholder(R.drawable.jandi_down_placeholder_img)
//                            .error(R.drawable.jandi_down_img_disable)
//                            .fitCenter()
//                            .crossfade(true)
//                            .load(thumbnailPhotoUrl);
                    break;
            }

            switch (sourceType) {

                case Google:
                case Dropbox:
                    imageViewPhotoFile.setOnClickListener(view ->
                            context.startActivity(new Intent(Intent
                                    .ACTION_VIEW, Uri.parse(originalFileUrl))));
                    break;
                default:
                    imageViewPhotoFile.setOnClickListener(view -> {
                        String optimizedImageUrl = getOptimizedImageUrl(context,
                                smallThumbnailUrl, mediumThumbnailUrl,
                                largeThumbnailUrl, originalFileUrl);

                        String imageUrl = BitmapUtil.getFileUrl(optimizedImageUrl);
                        PhotoViewActivity_
                                .intent(context)
                                .imageUrl(imageUrl)
                                .imageName(content.name)
                                .imageType(content.type)
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

    private boolean hasImageUrl(String small, String medium, String large, String original) {
        return !TextUtils.isEmpty(small)
                || !TextUtils.isEmpty(medium)
                || !TextUtils.isEmpty(large)
                || !TextUtils.isEmpty(original);
    }

    private String getOptimizedImageUrl(Context context,
                                        String small, String medium,
                                        String large, String original) {
        // XXHDPI 이상인 기기에서만 오리지널 파일을 로드
        int dpi = context.getResources().getDisplayMetrics().densityDpi;
        if (dpi > DisplayMetrics.DENSITY_XHIGH) {
            String url = original;
            return !TextUtils.isEmpty(url) ? url : getImageUrl(small, medium, large, original);
        }

        return getImageUrl(small, medium, large, original);
    }

    private String getImageUrl(String small, String medium, String large, String original) {
        // 라지 사이즈부터 조회(640 x 640)
        if (!TextUtils.isEmpty(large)) {
            return large;
        }

        // 중간 사이즈 (360 x 360)
        if (!TextUtils.isEmpty(medium)) {
            return medium;
        }

        // 원본 파일
        if (!TextUtils.isEmpty(original)) {
            return original;
        }

        return small;
    }
}
