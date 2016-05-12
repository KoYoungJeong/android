package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.util.ProfileUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.listener.BaseOnResourceReadyCallback;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

public class ImageMessageViewHolder extends BaseMessageViewHolder {

    public static final String TAG = ImageMessageViewHolder.class.getSimpleName();

    public static final int MIN_IMAGE_WIDTH = 75;
    public static final int MIN_IMAGE_HEIGHT = 75;

    public static final int IMAGE_WIDTH_LEFT_MARGIN = 46;
    public static final int IMAGE_WIDTH_RIGHT_MARGIN = 59;
    public static final int MAX_IMAGE_HEIGHT = 150;

    private final float MIN_WIDTH_RATIO;
    private final float MAX_WIDTH_RATIO;

    private SimpleDraweeView ivProfile;
    private TextView tvName;
    private SimpleDraweeView ivFileImage;
    private TextView tvFileName;
    private View vDisableLineThrough;

    private int minImageWidth;
    private int minImageHeight;
    private int maxImageWidth;
    private int maxImageHeight;
    private View vgFileImageWrapper;
    private TextView tvFileSize;
    private View vProfileCover;

    private ImageMessageViewHolder() {
        DisplayMetrics displayMetrics = JandiApplication.getContext().getResources().getDisplayMetrics();
        minImageWidth = getPixelFromDp(MIN_IMAGE_WIDTH, displayMetrics);
        minImageHeight = getPixelFromDp(MIN_IMAGE_HEIGHT, displayMetrics);
        maxImageWidth = displayMetrics.widthPixels
                - getPixelFromDp(IMAGE_WIDTH_LEFT_MARGIN, displayMetrics)
                - getPixelFromDp(IMAGE_WIDTH_RIGHT_MARGIN, displayMetrics);
        maxImageHeight = getPixelFromDp(MAX_IMAGE_HEIGHT, displayMetrics);
        MAX_WIDTH_RATIO = (float) maxImageWidth / (float) minImageHeight;
        MIN_WIDTH_RATIO = (float) minImageWidth / (float) maxImageHeight;
    }

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        if (hasProfile) {
            ivProfile = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_user_profile);
            vProfileCover = rootView.findViewById(R.id.v_message_user_profile_cover);
            tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
            vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);
        }

        vgFileImageWrapper = rootView.findViewById(R.id.vg_message_photo_wrapper);
        ivFileImage = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_photo);
        tvFileName = (TextView) rootView.findViewById(R.id.tv_image_message_file_name);
        tvFileSize = (TextView) rootView.findViewById(R.id.tv_file_size);

        initViewSizes();
    }

    @Override
    public int getLayoutId() {
        if (hasProfile) {
            return R.layout.item_message_image_v3;
        } else {
            return R.layout.item_message_image_collapse_v3;
        }
    }

    // 계속 계산하지 않도록
    private void initViewSizes() {
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        setMarginVisible();
        setTimeVisible();
        if (hasProfile) {
            ProfileUtil.setProfile(link.fromEntity, ivProfile, vProfileCover, tvName, vDisableLineThrough);
        }
        bindFileImage(link, teamId, roomId);
        setFileTitleBackground(link);
    }

    private void setFileTitleBackground(ResMessages.Link link) {
        long writerId = link.fromEntity;
        if (EntityManager.getInstance().isMe(writerId)) {
            tvFileName.setBackgroundResource(R.drawable.bg_round_bottom_blue_for_message);
        } else {
            tvFileName.setBackgroundResource(R.drawable.bg_round_bottom_white_for_message);
        }
    }

    private void bindFileImage(ResMessages.Link link, long teamId, long roomId) {
        if (!(link.message instanceof ResMessages.FileMessage)) {
            return;
        }

        long fromEntityId = link.fromEntity;

        EntityManager entityManager = EntityManager.getInstance();

        int unreadCount = UnreadCountUtil.getUnreadCount(
                teamId, roomId, link.id, fromEntityId, entityManager.getMe().getId());

        tvMessageBadge.setText(String.valueOf(unreadCount));

        if (unreadCount <= 0) {
            tvMessageBadge.setVisibility(View.GONE);
        } else {
            tvMessageBadge.setVisibility(View.VISIBLE);
        }

        tvMessageTime.setText(DateTransformator.getTimeStringForSimple(link.time));

        ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) link.message;
        ResMessages.FileContent fileContent = fileMessage.content;
        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(fileContent.serverUrl);

        if (TextUtils.equals(fileMessage.status, "archived")) {
            tvFileName.setText(R.string.jandi_deleted_file);
            ivFileImage.setImageURI(UriFactory.getResourceUri(R.drawable.file_icon_deleted));
            return;
        }

        tvFileName.setText(fileContent.title);

        if (!ImageUtil.hasImageUrl(fileContent)) {
            ImageLoader.newBuilder()
                    .actualScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                    .load(R.drawable.file_icon_img)
                    .into(ivFileImage);
            return;
        }

        // Google, Dropbox 파일이 인 경우
        if (isFileFromGoogleOrDropbox(sourceType)) {
            String serverUrl = fileContent.serverUrl;
            String icon = fileContent.icon;
            int mimeTypeIconImage = MimeTypeUtil.getMimeTypeIconImage(serverUrl, icon);
            ImageLoader.newBuilder()
                    .placeHolder(mimeTypeIconImage, ScalingUtils.ScaleType.CENTER_INSIDE)
                    .actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                    .load(UriFactory.getResourceUri(mimeTypeIconImage))
                    .into(ivFileImage);

        } else {
            String fileSize = FileUtil.fileSizeCalculation(fileContent.size);
            tvFileSize.setText(fileSize);

            String localFilePath = ImageUtil.getLocalFilePath(fileMessage.id);
            boolean isFromLocalFilePath = !TextUtils.isEmpty(localFilePath);

            final ResMessages.ThumbnailUrls extraInfo = fileContent.extraInfo;
            String remoteFilePth =
                    ImageUtil.getThumbnailUrl(extraInfo, ImageUtil.Thumbnails.LARGE);

            final ViewGroup.LayoutParams layoutParams = ivFileImage.getLayoutParams();

            ImageLoader.Builder imageRequestBuilder = ImageLoader.newBuilder();
            imageRequestBuilder.error(R.drawable.preview_no_img, ScalingUtils.ScaleType.CENTER_INSIDE);

            // 유효한 확장자가 아닌 경우, Local File Path 도 없고 Thumbnail Path 도 없는 경우
            boolean shouldSupportImageExtensions =
                    FileExtensionsUtil.shouldSupportImageExtensions(fileContent.ext);
            if (!shouldSupportImageExtensions
                    || (!isFromLocalFilePath && TextUtils.isEmpty(remoteFilePth))) {
                LogUtil.i(TAG, "Thumbnail's are empty.");

                layoutParams.height = maxImageHeight;
                ivFileImage.setLayoutParams(layoutParams);
                vgFileImageWrapper.setBackgroundColor(vgFileImageWrapper.getResources().getColor(R.color.jandi_messages_big_size_image_view_bg));

                imageRequestBuilder.actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE);
                imageRequestBuilder.load(R.drawable.preview_no_img)
                        .into(ivFileImage);
                return;
            }

            imageRequestBuilder.placeHolder(
                    R.drawable.comment_image_preview_download, ScalingUtils.ScaleType.CENTER_INSIDE);

            ImageLoadInfo imageInfo = getImageInfo(extraInfo);
            if (imageInfo.needCrop) {
                imageRequestBuilder.actualScaleType(ScalingUtils.ScaleType.CENTER_CROP);
            } else {
                imageRequestBuilder.actualScaleType(ScalingUtils.ScaleType.FIT_CENTER);
            }

            vgFileImageWrapper.setBackgroundColor(vgFileImageWrapper.getResources().getColor(R.color.jandi_messages_image_view_bg));

            layoutParams.width = imageInfo.width;
            layoutParams.height = imageInfo.height;
            ivFileImage.setLayoutParams(layoutParams);
            ivFileImage.setBackgroundColor(Color.TRANSPARENT);

            Uri uri = isFromLocalFilePath
                    ? UriFactory.getFileUri(localFilePath) : Uri.parse(remoteFilePth);

            imageRequestBuilder
                    .callback(new BaseOnResourceReadyCallback() {
                        @Override
                        public void onFail(Throwable cause) {
                            ivFileImage.setImageURI(UriFactory.getResourceUri(R.drawable.comment_no_img));
                            vgFileImageWrapper.setBackgroundColor(vgFileImageWrapper.getResources().getColor(R.color.jandi_messages_big_size_image_view_bg));
                        }
                    })
                    .load(uri)
                    .into(ivFileImage);
        }
    }

    private ImageLoadInfo getImageInfo(ResMessages.ThumbnailUrls extraInfo) {
        float width = maxImageWidth;
        float height = maxImageHeight;

        if (extraInfo != null && extraInfo.width > 0 && extraInfo.height > 0) {

            float extraInfoWidth = extraInfo.width;
            float extraInfoHeight = extraInfo.height;

            if (ImageUtil.isVerticalPhoto(extraInfo.orientation)) {
                float temp = extraInfoWidth;
                extraInfoWidth = extraInfoHeight;
                extraInfoHeight = temp;
            }

            float ratio = extraInfoWidth / extraInfoHeight;

            boolean needCrop = false;
            if (ratio > 1) {
                // 가로 > 세로
                if (ratio > MAX_WIDTH_RATIO) {
                    needCrop = true;
                    height = minImageHeight;
                    width = maxImageWidth;
                } else {
                    width = maxImageWidth;
                    height = width / ratio;
                }
            } else if (ratio < 1) {
                // 세로 > 가로
                if (ratio < MIN_WIDTH_RATIO) {
                    needCrop = true;
                    width = minImageWidth;
                    height = maxImageHeight;
                } else {
                    width = maxImageWidth;
                    height = width * ratio;
                }
            }

            return new ImageLoadInfo(needCrop, width, height);

        }
        return new ImageLoadInfo(false, width, height);
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        vgFileImageWrapper.setOnClickListener(itemClickListener);
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        vgFileImageWrapper.setOnLongClickListener(itemLongClickListener);
    }

    private int getPixelFromDp(int dp, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    private boolean isFileFromGoogleOrDropbox(MimeTypeUtil.SourceType sourceType) {
        return sourceType == MimeTypeUtil.SourceType.Google
                || sourceType == MimeTypeUtil.SourceType.Dropbox;
    }

    public void setHasBottomMargin(boolean hasBottomMargin) {
        this.hasBottomMargin = hasBottomMargin;
    }

    public static class Builder extends BaseViewHolderBuilder {

        public ImageMessageViewHolder build() {
            ImageMessageViewHolder imageViewHolder = new ImageMessageViewHolder();
            imageViewHolder.setHasBottomMargin(hasBottomMargin);
            return imageViewHolder;
        }
    }


    private static class ImageLoadInfo {
        private boolean needCrop;
        private int width;
        private int height;

        public ImageLoadInfo(boolean needCrop, float width, float height) {
            this.needCrop = needCrop;
            this.width = Math.round(width);
            this.height = Math.round(height);
        }

    }
}
