package com.tosslab.jandi.app.ui.filedetail.adapter.viewholder;

import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.UriUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.listener.SimpleRequestListener;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

/**
 * Created by tonyjs on 16. 1. 19..
 *
 * @see FileViewHolder
 */
public class ImageFileViewHolder extends FileViewHolder {

    private ImageView ivFileThumb;

    private View btnTapToView;

    private View vUnavailableIndicator;
    private OnImageFileClickListener onImageFileClickListener;

    private ImageFileViewHolder(View itemView, OnImageFileClickListener onImageFileClickListener) {
        super(itemView);
        this.onImageFileClickListener = onImageFileClickListener;
    }

    public static ImageFileViewHolder newInstance(ViewGroup parent, OnImageFileClickListener onImageFileClickListener) {
        return new ImageFileViewHolder(FileViewHolder.getItemView(parent), onImageFileClickListener);
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

        boolean hasImageUrl = ImageUtil.hasImageUrl(content);
        final String originalUrl = ImageUtil.getOriginalUrl(content);

        if (!hasImageUrl || !FileExtensionsUtil.shouldSupportImageExtensions(content.ext)) {
            ivFileThumb.setVisibility(View.GONE);
            ivFileThumb.setOnClickListener(null);
            vUnavailableIndicator.setVisibility(View.VISIBLE);
            return;
        }

        final long fileMessageId = fileMessage.id;

        ResMessages.ThumbnailUrls extraInfo = content.extraInfo;
        boolean hasThumbnailUrl = extraInfo != null && !TextUtils.isEmpty(extraInfo.largeThumbnailUrl);
        ivFileThumb.setOnClickListener(view -> moveToPhotoViewer(fileMessageId, fileMessage, false));

        String localFilePath = ImageUtil.getLocalFilePath(fileMessage.id);

        if (isDeleted(fileMessage.status)) {

            ivFileThumb.setImageResource(R.drawable.file_icon_delete_198);
            return;
        }

        if (!TextUtils.isEmpty(localFilePath)) {
            ImageLoader.newInstance()
                    .placeHolder(R.drawable.preview_img, ImageView.ScaleType.CENTER)
                    .actualImageScaleType(ImageView.ScaleType.FIT_CENTER)
                    .error(R.drawable.file_noimage, ImageView.ScaleType.FIT_CENTER)
                    .uri(UriUtil.getFileUri(localFilePath))
                    .into(ivFileThumb);
            return;
        }

        if (hasThumbnailUrl) {
            ImageLoader.newInstance()
                    .placeHolder(R.drawable.preview_img, ImageView.ScaleType.CENTER)
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
                .placeHolder(R.drawable.preview_img, ImageView.ScaleType.CENTER)
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
                        showTapToViewLayout(fileMessageId, fileMessage);
                        return true;
                    }
                })
                .uri(originalUri)
                .into(ivFileThumb);
    }

    private void showTapToViewLayout(long fileMessageId, ResMessages.FileMessage fileMessage) {

        btnTapToView.setVisibility(View.VISIBLE);

        ivFileThumb.setOnClickListener(null);

        btnTapToView.setOnClickListener(v -> {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewOriginalImage);

            btnTapToView.setVisibility(View.GONE);

            moveToPhotoViewer(fileMessageId, fileMessage, true);
        });
    }

    private void moveToPhotoViewer(long fileMessageId, ResMessages.FileMessage fileMessage,
                                   boolean shouldOpenImmediately) {
        if (onImageFileClickListener != null) {
            onImageFileClickListener.onImageFileClick(fileMessageId, fileMessage, shouldOpenImmediately);
        }
    }

    public void setOnImageFileClickListener(OnImageFileClickListener onImageFileClickListener) {
        this.onImageFileClickListener = onImageFileClickListener;
    }

    public interface OnImageFileClickListener {
        void onImageFileClick(long fileMessageId, ResMessages.FileMessage fileMessage,
                              boolean shouldOpenImmediately);
    }
}
