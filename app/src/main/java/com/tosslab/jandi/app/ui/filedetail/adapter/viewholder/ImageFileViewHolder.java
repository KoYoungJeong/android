package com.tosslab.jandi.app.ui.filedetail.adapter.viewholder;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity_;
import com.tosslab.jandi.app.ui.photo.widget.CircleProgressBar;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.image.ImageDownloadTracker;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

/**
 * Created by tonyjs on 16. 1. 19..
 *
 * @see FileViewHolder
 */
public class ImageFileViewHolder extends FileViewHolder {

    private long roomId;

    private SimpleDraweeView ivFileThumb;

    private View btnTapToView;
    private ViewGroup vgProgressBar;
    private CircleProgressBar progressBar;
    private TextView tvPercentage;

    private View vUnavailableIndicator;
    private CountDownTimer progressTimer;

    private ImageFileViewHolder(View itemView, long roomId) {
        super(itemView);
        this.roomId = roomId;
    }

    public static ImageFileViewHolder newInstance(ViewGroup parent, long roomId) {
        return new ImageFileViewHolder(FileViewHolder.getItemView(parent), roomId);
    }

    @Override
    public void addContentView(ViewGroup parent) {
        View contentView = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_file_detail_image_content, parent, true);
        ivFileThumb = (SimpleDraweeView) contentView.findViewById(R.id.iv_file_detail_thumb);
        btnTapToView = contentView.findViewById(R.id.vg_file_detail_tap_to_view);
        vgProgressBar = (ViewGroup) contentView.findViewById(R.id.vg_file_detail_progress);
        progressBar = (CircleProgressBar) contentView.findViewById(R.id.progress_file_detail);
        tvPercentage = (TextView) contentView.findViewById(R.id.tv_file_detail_percentage);
        vUnavailableIndicator = contentView.findViewById(R.id.vg_file_detail_no_image);

        setupProgress();
    }

    @Override
    public void bindFileContent(ResMessages.FileMessage fileMessage) {
        final ResMessages.FileContent content = fileMessage.content;

        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(content.serverUrl);

        boolean hasImageUrl = ImageUtil.hasImageUrl(content);
        final String originalUrl =
                ImageUtil.getThumbnailUrlOrOriginal(content, ImageUtil.Thumbnails.ORIGINAL);

        if (MimeTypeUtil.isFileFromGoogleOrDropbox(sourceType)) {
            int resourceId = sourceType == MimeTypeUtil.SourceType.Google
                    ? R.drawable.jandi_down_placeholder_google
                    : R.drawable.jandi_down_placeholder_dropbox;

            ImageLoader.newBuilder()
                    .actualScaleType(ScalingUtils.ScaleType.FIT_XY)
                    .load(resourceId)
                    .into(ivFileThumb);

            if (hasImageUrl) {
                ivFileThumb.setOnClickListener(view -> {
                    getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(originalUrl)));
                    AnalyticsUtil.sendEvent(
                            AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewPhoto);
                });
            } else {
                ivFileThumb.setOnClickListener(null);
            }

            return;
        }

        if (!hasImageUrl || !FileExtensionsUtil.shouldSupportImageExtensions(content.ext)) {
            ivFileThumb.setVisibility(View.GONE);
            ivFileThumb.setOnClickListener(null);
            vUnavailableIndicator.setVisibility(View.VISIBLE);
            return;
        }

        final long fileMessageId = fileMessage.id;

        String localFilePath = ImageUtil.getLocalFilePath(fileMessage.id);

        ResMessages.ThumbnailUrls extraInfo = content.extraInfo;
        boolean hasThumbnailUrl = extraInfo != null && !TextUtils.isEmpty(extraInfo.largeThumbnailUrl);
        ivFileThumb.setOnClickListener(view -> moveToPhotoViewer(fileMessageId, content));

        Uri originalUri = Uri.parse(originalUrl);

        boolean hasDownloadHistory = ImageUtil.hasCache(originalUri) ||
                (ImageDownloadTracker.getInstance()
                        .getStatus(originalUri) != ImageDownloadTracker.Status.PENDING);

        if (TextUtils.isEmpty(localFilePath)
                && !hasThumbnailUrl
                && !hasDownloadHistory) {

            showTapToViewLayout(originalUri, fileMessageId, content);

        } else {
            final ImageLoader.Builder builder = ImageLoader.newBuilder();
            builder.placeHolder(R.drawable.comment_image_preview_download, ScalingUtils.ScaleType.FIT_XY)
                    .actualScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                    .error(R.drawable.file_noimage, ScalingUtils.ScaleType.FIT_CENTER);

            final Uri uri = !TextUtils.isEmpty(localFilePath)
                    ? UriFactory.getFileUri(localFilePath)
                    : hasThumbnailUrl
                    ? Uri.parse(extraInfo.largeThumbnailUrl) : originalUri;
            builder.controllerListener(new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id,
                                            ImageInfo imageInfo, Animatable animatable) {
                    ImageDownloadTracker.getInstance().put(uri, ImageDownloadTracker.Status.COMPLETED);
                }
            });
            builder.load(uri)
                    .into(ivFileThumb);
            ImageDownloadTracker.getInstance().put(uri, ImageDownloadTracker.Status.IN_PROGRESS);
        }
    }

    private void showTapToViewLayout(final Uri originalUri,
                                     long fileMessageId, ResMessages.FileContent content) {

        btnTapToView.setVisibility(View.VISIBLE);

        ivFileThumb.setOnClickListener(null);

        btnTapToView.setOnClickListener(v -> {
            btnTapToView.setVisibility(View.GONE);
            vgProgressBar.setVisibility(View.VISIBLE);

            loadImageWithCallback(originalUri);

            ivFileThumb.setOnClickListener(
                    view -> moveToPhotoViewer(fileMessageId, content));
        });
    }

    private void loadImageWithCallback(final Uri uri) {
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

                ImageDownloadTracker.getInstance().put(uri, ImageDownloadTracker.Status.COMPLETED);
            }
        });
        builder.load(uri)
                .into(ivFileThumb);
        ImageDownloadTracker.getInstance().put(uri, ImageDownloadTracker.Status.IN_PROGRESS);

        progressTimer.start();
    }

    private void moveToPhotoViewer(long fileMessageId, ResMessages.FileContent content) {
        Context context = getContext();

        if (roomId > 0) {
            CarouselViewerActivity_.intent(context)
                    .mode(CarouselViewerActivity.CAROUSEL_MODE)
                    .roomId(roomId)
                    .startLinkId(fileMessageId)
                    .start();
        } else {
            String thumbUrl = ImageUtil.getThumbnailUrl(content.extraInfo, ImageUtil.Thumbnails.THUMB);
            CarouselViewerActivity_.intent(context)
                    .mode(CarouselViewerActivity.SINGLE_IMAGE_MODE)
                    .imageExt(content.ext)
                    .imageOriginUrl(content.fileUrl)
                    .imageThumbUrl(thumbUrl)
                    .imageType(content.type)
                    .imageName(content.name)
                    .imageSize(content.size)
                    .start();
        }

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewPhoto);
    }

    private void setupProgress() {
        Resources resources = getContext().getResources();
        int progressWidth = (int) (resources.getDisplayMetrics().density * 3);
        progressBar.setBgStrokeWidth(progressWidth);
        progressBar.setProgressStrokeWidth(progressWidth);
        progressBar.setBgColor(resources.getColor(R.color.white));
        progressBar.setProgressColor(resources.getColor(R.color.jandi_accent_color));
        progressBar.setMax(100);
    }
}
