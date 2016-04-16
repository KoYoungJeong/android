package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

import de.greenrobot.event.EventBus;

public class ImageMessageViewHolder extends BaseMessageViewHolder {

    public static final String TAG = ImageMessageViewHolder.class.getSimpleName();

    public static final int MIN_IMAGE_WIDTH = 75;
    public static final int MIN_IMAGE_HEIGHT = 75;

    public static final int IMAGE_WIDTH_LEFT_MARGIN = 46;
    public static final int IMAGE_WIDTH_RIGHT_MARGIN = 59;
    public static final int MAX_IMAGE_HEIGHT = 150;

    private SimpleDraweeView ivProfile;
    private TextView tvName;
    private SimpleDraweeView ivFileImage;
    private TextView tvFileName;
    private View vDisableCover;
    private View vDisableLineThrough;
    private Context context;

    private int minImageWidth;
    private int minImageHeight;
    private int maxImageWidth;
    private int maxImageHeight;
    private RelativeLayout vgFileImageWrapper;
    private TextView tvFileSize;

    private ImageMessageViewHolder() {
    }

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        ivProfile = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_user_profile);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
        vDisableCover = rootView.findViewById(R.id.v_entity_listitem_warning);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);

        vgFileImageWrapper = (RelativeLayout) rootView.findViewById(R.id.vg_message_photo_wrapper);
        ivFileImage = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_photo);
        tvFileName = (TextView) rootView.findViewById(R.id.tv_image_message_file_name);
        tvFileSize = (TextView) rootView.findViewById(R.id.tv_file_size);

        context = rootView.getContext();

        initViewSizes();
    }

    @Override
    protected void initObjects() {
        vgMessageContent.setVisibility(View.GONE);
        vgStickerMessageContent.setVisibility(View.GONE);
        vgFileMessageContent.setVisibility(View.GONE);
        vgImageMessageContent.setVisibility(View.VISIBLE);
    }

    // 계속 계산하지 않도록
    private void initViewSizes() {
        minImageWidth = getPixelFromDp(MIN_IMAGE_WIDTH);
        minImageHeight = getPixelFromDp(MIN_IMAGE_HEIGHT);
        maxImageWidth = getDisplayWidth()
                - getPixelFromDp(IMAGE_WIDTH_LEFT_MARGIN) - getPixelFromDp(IMAGE_WIDTH_RIGHT_MARGIN);
        maxImageHeight = getPixelFromDp(MAX_IMAGE_HEIGHT);
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        setProfileInfos(link);
        bindFileImage(link);
    }

    public void setProfileInfos(ResMessages.Link link) {
        long fromEntityId = link.fromEntity;

        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entity = entityManager.getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = entity.getUserLargeProfileUrl();

        ImageUtil.loadProfileImage(ivProfile, profileUrl, R.drawable.profile_img);

        if (fromEntity != null && entity.isEnabled()) {
            tvName.setTextColor(context.getResources().getColor(R.color.jandi_messages_name));
            vDisableCover.setVisibility(View.GONE);
            vDisableLineThrough.setVisibility(View.GONE);
        } else {
            tvName.setTextColor(
                    tvName.getResources().getColor(R.color.deactivate_text_color));
            vDisableCover.setVisibility(View.VISIBLE);
            vDisableLineThrough.setVisibility(View.VISIBLE);
        }

        tvName.setText(fromEntity.name);
        ivProfile.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Image)));
        tvName.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Name)));
    }

    private void bindFileImage(ResMessages.Link link) {
        if (!(link.message instanceof ResMessages.FileMessage)) {
            return;
        }

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
            imageRequestBuilder.error(R.drawable.preview_no_img, ScalingUtils.ScaleType.FIT_XY);

            // 유효한 확장자가 아닌 경우, Local File Path 도 없고 Thumbnail Path 도 없는 경우
            boolean shouldSupportImageExtensions =
                    FileExtensionsUtil.shouldSupportImageExtensions(fileContent.ext);
            if (!shouldSupportImageExtensions
                    || (!isFromLocalFilePath && TextUtils.isEmpty(remoteFilePth))) {
                LogUtil.i(TAG, "Thumbnail's are empty.");

                ViewGroup.LayoutParams wrapperLayoutParams = vgFileImageWrapper.getLayoutParams();
                wrapperLayoutParams.height = maxImageHeight;
                vgFileImageWrapper.setLayoutParams(wrapperLayoutParams);
                vgFileImageWrapper.setBackgroundDrawable(JandiApplication.getContext()
                        .getResources().getDrawable(R.drawable.bg_round_top_green_for_message));

                imageRequestBuilder.actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE);
                imageRequestBuilder.load(R.drawable.preview_no_img)
                        .into(ivFileImage);
                return;
            }

            imageRequestBuilder.placeHolder(
                    R.drawable.comment_image_preview_download, ScalingUtils.ScaleType.CENTER_INSIDE);

            int width = maxImageWidth;
            int height = maxImageHeight;

            if (extraInfo != null && extraInfo.width > 0 && extraInfo.height > 0) {

                int extraInfoWidth = extraInfo.width;
                int extraInfoHeight = extraInfo.height;

                if (ImageUtil.isVerticalPhoto(extraInfo.orientation)) {
                    int temp = extraInfoWidth;
                    extraInfoWidth = extraInfoHeight;
                    extraInfoHeight = temp;
                }

                if (extraInfo.height < minImageHeight) {
                    height = minImageHeight;
                } else if (extraInfo.height > maxImageHeight) {
                    height = maxImageHeight;
                } else {
                    height = extraInfoHeight;
                }

                int convertedWidthByRatio = (int) ((float) extraInfoWidth * ((float) height / (float) extraInfoHeight));

                if (convertedWidthByRatio < minImageWidth) {
                    width = minImageWidth;
                } else if (convertedWidthByRatio > maxImageWidth) {
                    width = maxImageWidth;
                } else {
                    width = convertedWidthByRatio;
                }

            }

            ViewGroup.LayoutParams wrapperLayoutParams = vgFileImageWrapper.getLayoutParams();
            wrapperLayoutParams.height = height;
            vgFileImageWrapper.setLayoutParams(wrapperLayoutParams);
            vgFileImageWrapper.setBackgroundDrawable(JandiApplication.getContext()
                    .getResources().getDrawable(R.drawable.bg_round_top_gray_for_message));

            layoutParams.width = width;
            layoutParams.height = height;
            ivFileImage.setLayoutParams(layoutParams);

            Uri uri = isFromLocalFilePath
                    ? UriFactory.getFileUri(localFilePath) : Uri.parse(remoteFilePth);

            imageRequestBuilder
                    .actualScaleType(ScalingUtils.ScaleType.FIT_XY)
                    .load(uri)
                    .into(ivFileImage);
        }
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        super.setOnItemClickListener(itemClickListener);
        vgImageMessageContent.setOnClickListener(itemClickListener);
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        super.setOnItemLongClickListener(itemLongClickListener);
        vgImageMessageContent.setOnLongClickListener(itemLongClickListener);
    }

    private int getPixelFromDp(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    private int getDisplayWidth() {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    private boolean isFileFromGoogleOrDropbox(MimeTypeUtil.SourceType sourceType) {
        return sourceType == MimeTypeUtil.SourceType.Google
                || sourceType == MimeTypeUtil.SourceType.Dropbox;
    }

    public void setHasBottomMargin(boolean hasBottomMargin) {
        this.hasBottomMargin = hasBottomMargin;
    }

    public static class Builder extends BaseViewHolderBuilder {
//        private boolean hasBottomMargin = false;
//
//        public Builder setHasBottomMargin(boolean hasBottomMargin) {
//            this.hasBottomMargin = hasBottomMargin;
//            return this;
//        }

        public ImageMessageViewHolder build() {
            ImageMessageViewHolder imageViewHolder = new ImageMessageViewHolder();
            imageViewHolder.setHasBottomMargin(hasBottomMargin);
            return imageViewHolder;
        }
    }

}
