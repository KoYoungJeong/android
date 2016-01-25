package com.tosslab.jandi.app.ui.filedetail.fileinfo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity_;
import com.tosslab.jandi.app.ui.photo.PhotoViewActivity_;
import com.tosslab.jandi.app.ui.photo.widget.CircleProgressBar;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
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

    private final long roomId;
    private Context context;
    private CountDownTimer progressTimer;

    public ImageThumbLoader(ImageView ivFileTypeIcon, ViewGroup vgDetailPhoto, SimpleDraweeView ivFilePhoto,
                            ViewGroup vgTapToViewOriginal, long roomId) {
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

            ImageLoader.newBuilder()
                    .actualScaleType(ScalingUtils.ScaleType.FIT_XY)
                    .load(resourceId)
                    .into(ivFilePhoto);

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

        if (!hasImageUrl || !FileExtensionsUtil.shouldSupportImageExtensions(content.ext)) {
            ImageLoader.newBuilder()
                    .actualScaleType(ScalingUtils.ScaleType.FIT_XY)
                    .load(R.drawable.file_noimage)
                    .into(ivFilePhoto);
            ivFilePhoto.setOnClickListener(null);
            return;
        }

        final long fileMessageId = fileMessage.id;

        String localFilePath = ImageUtil.getLocalFilePath(fileMessage.id);

        ResMessages.ThumbnailUrls extraInfo = content.extraInfo;
        boolean hasThumbnailUrl = extraInfo != null && !TextUtils.isEmpty(extraInfo.largeThumbnailUrl);
        ivFilePhoto.setOnClickListener(view -> moveToPhotoViewer(fileMessageId, content));

        Uri originalUri = Uri.parse(originalUrl);
        if (TextUtils.isEmpty(localFilePath)
                && !hasThumbnailUrl
                && !ImageUtil.hasCache(originalUri)) {

            showTapToViewLayout(originalUri, fileMessageId, content);

        } else {
            final ImageLoader.Builder builder = ImageLoader.newBuilder();
            builder.placeHolder(R.drawable.comment_image_preview_download, ScalingUtils.ScaleType.FIT_XY);
            builder.actualScaleType(ScalingUtils.ScaleType.FIT_CENTER);
            builder.error(R.drawable.file_noimage, ScalingUtils.ScaleType.FIT_CENTER);

            Uri uri = !TextUtils.isEmpty(localFilePath)
                    ? UriFactory.getFileUri(localFilePath)
                    : hasThumbnailUrl
                    ? Uri.parse(extraInfo.largeThumbnailUrl) : originalUri;

            int width = ImageUtil.STANDARD_IMAGE_SIZE;
            int height = ImageUtil.STANDARD_IMAGE_SIZE;

            boolean hasSizeInfo = extraInfo != null && extraInfo.width > 0 && extraInfo.height > 0;
            if (hasSizeInfo) {
                Pair<Integer, Integer> widthAndHeight
                        = updateViewSize(extraInfo.width, extraInfo.height, extraInfo.orientation);
                width = widthAndHeight.first;
                height = widthAndHeight.second;
            }

            builder.resize(width, height);
            if (!hasSizeInfo) {
                builder.controllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id,
                                                ImageInfo imageInfo, Animatable animatable) {
                        updateViewSize(imageInfo.getWidth(), imageInfo.getHeight());
                    }
                });
            }
            builder.load(uri).into(ivFilePhoto);
        }
    }

    private void showTapToViewLayout(final Uri originalUri,
                                     long fileMessageId, ResMessages.FileContent content) {

        vgTapToViewOriginal.setVisibility(View.VISIBLE);

        ivFilePhoto.setOnClickListener(null);

        vgTapToViewOriginal.setOnClickListener(v -> {
            vgTapToViewOriginal.setVisibility(View.GONE);
            vgProgressBar.setVisibility(View.VISIBLE);

            loadImageWithCallback(originalUri);

            ivFilePhoto.setOnClickListener(view -> moveToPhotoViewer(fileMessageId, content));
        });
    }

    private void loadImageWithCallback(Uri uri) {
        final int millisInFuture = 6000;
        progressTimer = new CountDownTimer(millisInFuture, 120) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (progressBar == null) {
                    progressTimer.cancel();
                    return;
                }

                int gap = (int) (millisInFuture - millisUntilFinished);

                int percentage = (int) ((gap / (float) millisInFuture) * 100);
                percentage = Math.min(percentage, 99);
                progressBar.setProgress(percentage);
                tvPercentage.setText(percentage + "%");
            }

            @Override
            public void onFinish() {
                if (progressBar == null || tvPercentage == null) {
                    return;
                }
                progressBar.setProgress(99);
                tvPercentage.setText(99 + "%");
            }
        };

        ImageLoader.Builder builder = ImageLoader.newBuilder();
        builder.actualScaleType(ScalingUtils.ScaleType.FIT_CENTER);
        builder.error(R.drawable.file_noimage, ScalingUtils.ScaleType.FIT_XY);
        builder.controllerListener(new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                vgProgressBar.setVisibility(View.GONE);

                progressTimer.cancel();

                progressBar.setProgress(100);
                tvPercentage.setText(100 + "%");

                updateViewSize(imageInfo.getWidth(), imageInfo.getHeight());
            }
        });

        builder.load(uri).into(ivFilePhoto);
        progressTimer.start();
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

        ViewParent parent = vgDetailPhoto.getParent();
        int margins = 0;
        if (parent != null) {
            View parentView = (View) parent;
            margins = parentView.getPaddingLeft() + parentView.getPaddingRight()
                    + vgDetailPhoto.getPaddingLeft() + vgDetailPhoto.getPaddingRight();
            ViewGroup.LayoutParams layoutParams1 = parentView.getLayoutParams();
            if (layoutParams1 instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams parentMargin = (ViewGroup.MarginLayoutParams) layoutParams1;
                margins = margins + parentMargin.leftMargin + parentMargin.rightMargin;
            }

        }
        ViewGroup.LayoutParams layoutParams1 = vgDetailPhoto.getLayoutParams();
        if (layoutParams1 instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams photoMargin = (ViewGroup.MarginLayoutParams) layoutParams1;
            margins += (photoMargin.leftMargin + photoMargin.rightMargin);
        }

        int viewWidth = vgDetailPhoto.getResources().getDisplayMetrics().widthPixels - margins;
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

    private void moveToPhotoViewer(long fileMessageId, ResMessages.FileContent content) {
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
                    .extensions(content.ext)
                    .originalUrl(content.fileUrl)
                    .imageName(content.name)
                    .imageType(content.type)
                    .start();
        }
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewPhoto);
    }

}
