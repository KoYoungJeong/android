package com.tosslab.jandi.app.ui.filedetail.adapter.viewholder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.RequestShowCarouselViewerEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity_;
import com.tosslab.jandi.app.utils.UriUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.listener.SimpleRequestListener;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by tonyjs on 16. 1. 19..
 *
 * @see FileViewHolder
 */
public class ImageFileViewHolder extends FileViewHolder {

    private ImageView ivFileThumb;

    private View btnTapToView;

    private View vUnavailableIndicator;

    private ImageFileViewHolder(View itemView) {
        super(itemView);
    }

    public static ImageFileViewHolder newInstance(ViewGroup parent) {
        return new ImageFileViewHolder(FileViewHolder.getItemView(parent));
    }

    @Override
    public void addContentView(ViewGroup parent) {
        View contentView = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_file_detail_image_content, parent, true);
        ivFileThumb = (ImageView) contentView.findViewById(R.id.iv_file_detail_thumb);
        btnTapToView = contentView.findViewById(R.id.vg_file_detail_tap_to_view);
        vUnavailableIndicator = contentView.findViewById(R.id.vg_file_detail_no_image);
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
            ImageLoader.loadFromResources(ivFileThumb, resourceId);

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
        ivFileThumb.setOnClickListener(view -> moveToPhotoViewer(fileMessageId, content, false));

        String localFilePath = ImageUtil.getLocalFilePath(fileMessage.id);
        if (!TextUtils.isEmpty(localFilePath)) {
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

        ImageLoader.newInstance()
                // cache 되어 있는지 확인하기 위해 네트워킹 작업이 실행되면 exception 발생시킨다.
                .blockNetworking(true)
                .placeHolder(R.drawable.comment_image_preview_download, ImageView.ScaleType.FIT_XY)
                .actualImageScaleType(ImageView.ScaleType.FIT_CENTER)
                .listener(new SimpleRequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onResourceReady(GlideDrawable glideDrawable,
                                                   Uri model, Target<GlideDrawable> target,
                                                   boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        btnTapToView.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onException(Exception e, Uri model,
                                               Target<GlideDrawable> target,
                                               boolean isFirstResource) {
                        // cache 가 되어 있지 않음
                        showTapToViewLayout(fileMessageId, content);
                        return true;
                    }
                })
                .uri(originalUri)
                .into(ivFileThumb);
    }

    private void showTapToViewLayout(long fileMessageId, ResMessages.FileContent content) {

        btnTapToView.setVisibility(View.VISIBLE);

        ivFileThumb.setOnClickListener(null);

        btnTapToView.setOnClickListener(v -> {
            btnTapToView.setVisibility(View.GONE);

            moveToPhotoViewer(fileMessageId, content, true);
        });
    }

    private void moveToPhotoViewer(long fileMessageId, ResMessages.FileContent content,
                                   boolean shouldOpenImmediately) {
        EventBus.getDefault().post(
                new RequestShowCarouselViewerEvent(fileMessageId, content, shouldOpenImmediately));
    }
}
