package com.tosslab.jandi.app.ui.filedetail.adapter.viewholder;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity_;
import com.tosslab.jandi.app.ui.photo.widget.CircleProgressBar;
import com.tosslab.jandi.app.utils.UriUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.image.ImageDownloadTracker;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.listener.SimpleRequestListener;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.image.loader.ThrowIOExceptionStreamLoader;
import com.tosslab.jandi.app.utils.image.target.DynamicImageViewTarget;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

/**
 * Created by tonyjs on 16. 1. 19..
 *
 * @see FileViewHolder
 */
public class ImageFileViewHolder extends FileViewHolder {

    private long roomId;

    private ImageView ivFileThumb;

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
        ivFileThumb = (ImageView) contentView.findViewById(R.id.iv_file_detail_thumb);
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

            ivFileThumb.setScaleType(ImageView.ScaleType.FIT_XY);
            ivFileThumb.setImageResource(resourceId);

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

        ResMessages.ThumbnailUrls extraInfo = content.extraInfo;
        boolean hasThumbnailUrl = extraInfo != null && !TextUtils.isEmpty(extraInfo.largeThumbnailUrl);
        ivFileThumb.setOnClickListener(view -> moveToPhotoViewer(fileMessageId, content));

        int width = ivFileThumb.getWidth();
        int height = ivFileThumb.getHeight();
        int paramWidth = ivFileThumb.getLayoutParams().width;
        int paramHeight = ivFileThumb.getLayoutParams().height;

        Log.d("tony", String.format("width = %d, height = %d, paramWidth = %d, paramHeight = %d", width, height, paramWidth, paramHeight));

        String localFilePath = ImageUtil.getLocalFilePath(fileMessage.id);
        if (!TextUtils.isEmpty(localFilePath)) {
//            Glide.with(ivFileThumb.getContext())
//                    .load(UriUtil.getFileUri(localFilePath))
//                    .placeholder(R.drawable.comment_image_preview_download)
//                    .error(R.drawable.preview_no_img)
//                    .into(ivFileThumb);

            ImageLoader.newInstance()
                    .placeHolder(R.drawable.comment_image_preview_download, ImageView.ScaleType.FIT_XY)
                    .actualImageScaleType(ImageView.ScaleType.FIT_CENTER)
                    .error(R.drawable.file_noimage, ImageView.ScaleType.FIT_CENTER)
                    .uri(UriUtil.getFileUri(localFilePath))
                    .into(ivFileThumb);
            return;
        }

        if (hasThumbnailUrl) {
            ImageLoader.newInstance()
                    .placeHolder(R.drawable.comment_image_preview_download, ImageView.ScaleType.FIT_XY)
                    .actualImageScaleType(ImageView.ScaleType.FIT_CENTER)
                    .error(R.drawable.file_noimage, ImageView.ScaleType.FIT_CENTER)
                    .uri(Uri.parse(extraInfo.largeThumbnailUrl))
                    .into(ivFileThumb);
            return;
        }

        Uri originalUri = Uri.parse(originalUrl);

        Glide.with(ivFileThumb.getContext())
                // cache 되어 있는지 확인하기 위해 네트워킹 작업이 실행되면 exception 발생시킨다.
                .using(new ThrowIOExceptionStreamLoader<Uri>())
                .load(originalUri)
                .placeholder(R.drawable.comment_image_preview_download)
                .listener(new SimpleRequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model,
                                               Target<GlideDrawable> target,
                                               boolean isFirstResource) {
                        // cache 가 되어 있지 않음
                        showTapToViewLayout(originalUri, fileMessageId, content);
                        return true;
                    }
                })
                .into(DynamicImageViewTarget.newBuilder()
                        .placeHolderScaleType(ImageView.ScaleType.FIT_XY)
                        .actualImageScaleType(ImageView.ScaleType.FIT_CENTER)
                        .build(ivFileThumb));
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

        ImageLoader loader = ImageLoader.newInstance();
        loader.actualImageScaleType(ImageView.ScaleType.FIT_CENTER);
        loader.error(R.drawable.file_noimage, ImageView.ScaleType.FIT_XY);
        loader.listener(new SimpleRequestListener<Uri, GlideDrawable>() {
            @Override
            public boolean onResourceReady(GlideDrawable glideDrawable,
                                           Uri model, Target<GlideDrawable> target,
                                           boolean isFromMemoryCache, boolean isFirstResource) {
                vgProgressBar.setVisibility(View.GONE);

                progressTimer.cancel();

                progressBar.setProgress(100);
                tvPercentage.setText(100 + "%");

                ImageDownloadTracker.getInstance().put(uri, ImageDownloadTracker.Status.COMPLETED);
                return false;
            }
        });

        loader.uri(uri).into(ivFileThumb);

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
