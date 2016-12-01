package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.util.ProfileUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.UriUtil;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.listener.SimpleRequestListener;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

import rx.android.schedulers.AndroidSchedulers;

public class ImageMessageViewHolder extends BaseMessageViewHolder {

    public static final String TAG = ImageMessageViewHolder.class.getSimpleName();

    public static final int MIN_IMAGE_WIDTH = 75;
    public static final int MIN_IMAGE_HEIGHT = 75;

    public static final int IMAGE_WIDTH_LEFT_MARGIN = 46;
    public static final int IMAGE_WIDTH_RIGHT_MARGIN = 59;
    public static final int MAX_IMAGE_HEIGHT = 150;

    private final float MIN_WIDTH_RATIO;
    private final float MAX_WIDTH_RATIO;

    private ImageView ivProfile;
    private TextView tvName;
    private ImageView ivFileImage;
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
            ivProfile = (ImageView) rootView.findViewById(R.id.iv_message_user_profile);
            vProfileCover = rootView.findViewById(R.id.v_message_user_profile_cover);
            tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
            vDisableLineThrough = rootView.findViewById(R.id.iv_name_line_through);
        }

        vgFileImageWrapper = rootView.findViewById(R.id.vg_message_photo_wrapper);
        ivFileImage = (ImageView) rootView.findViewById(R.id.iv_message_photo);
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
        if (TeamInfoLoader.getInstance().getMyId() == writerId) {
            tvFileName.setBackgroundResource(R.drawable.bg_round_bottom_for_item_name_mine);
        } else {
            tvFileName.setBackgroundResource(R.drawable.bg_round_bottom_for_item_name);
        }
    }

    private void bindFileImage(ResMessages.Link link, long teamId, long roomId) {
        if (!(link.message instanceof ResMessages.FileMessage)) {
            return;
        }

        long fromEntityId = link.fromEntity;


        UnreadCountUtil.getUnreadCount(
                teamId, roomId, link.id, fromEntityId, TeamInfoLoader.getInstance().getMyId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(unreadCount -> {

                    tvMessageBadge.setText(String.valueOf(unreadCount));

                    if (unreadCount <= 0) {
                        tvMessageBadge.setVisibility(View.GONE);
                    } else {
                        tvMessageBadge.setVisibility(View.VISIBLE);
                    }
                });


        tvMessageTime.setText(DateTransformator.getTimeStringForSimple(link.time));

        ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) link.message;
        ResMessages.FileContent fileContent = fileMessage.content;
        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(fileContent.serverUrl);

        if (TextUtils.equals(fileMessage.status, "archived")) {
            tvFileName.setText(R.string.jandi_deleted_file);
            setImageViewSizeToDefault();
            ImageLoader.loadFromResources(ivFileImage, R.drawable.file_icon_deleted_135);
            return;
        }

        tvFileName.setText(fileContent.title);

        if (!ImageUtil.hasImageUrl(fileContent)) {
            setImageViewSizeToDefault();
            ivFileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ImageLoader.loadFromResources(ivFileImage, R.drawable.file_icon_img);
            return;
        }

        // Google, Dropbox 파일이 인 경우
        if (isFileFromGoogleOrDropbox(sourceType)) {
            setImageViewSizeToDefault();
            String serverUrl = fileContent.serverUrl;
            String icon = fileContent.icon;
            int mimeTypeIconImage = MimeTypeUtil.getMimeTypeIconImage(serverUrl, icon, SourceTypeUtil.TYPE_C);
            ivFileImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ImageLoader.loadFromResources(ivFileImage, mimeTypeIconImage);
            tvFileSize.setVisibility(View.INVISIBLE);
        } else {
            if (fileContent.size > 0) {
                String fileSize = FileUtil.formatFileSize(fileContent.size);
                tvFileSize.setText(fileSize);
                tvFileSize.setVisibility(View.VISIBLE);
            } else {
                tvFileSize.setVisibility(View.INVISIBLE);
            }

            String localFilePath = ImageUtil.getLocalFilePath(fileMessage.id);
            boolean isFromLocalFilePath = !TextUtils.isEmpty(localFilePath);

            final ResMessages.ThumbnailUrls extraInfo = fileContent.extraInfo;
            String remoteFilePath =
                    ImageUtil.getThumbnailUrl(extraInfo, ImageUtil.Thumbnails.LARGE);

            final ViewGroup.LayoutParams layoutParams = ivFileImage.getLayoutParams();

            // 유효한 확장자가 아닌 경우, Local File Path 도 없고 Thumbnail Path 도 없는 경우
            boolean shouldSupportImageExtensions =
                    FileExtensionsUtil.shouldSupportImageExtensions(fileContent.ext);
            Resources resources = vgFileImageWrapper.getResources();
            final int bigSizeImageBackgroundColor = resources
                    .getColor(R.color.jandi_messages_big_size_image_view_bg);
            if (!shouldSupportImageExtensions
                    || (!isFromLocalFilePath && TextUtils.isEmpty(remoteFilePath))) {
                LogUtil.i(TAG, "Thumbnail's are empty.");

                layoutParams.height = maxImageHeight;
                ivFileImage.setLayoutParams(layoutParams);
                vgFileImageWrapper.setBackgroundColor(bigSizeImageBackgroundColor);

                ivFileImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ImageLoader.loadFromResources(ivFileImage, R.drawable.preview_no_img);
                return;
            }

            ImageLoader loader = ImageLoader.newInstance();
            loader.error(R.drawable.comment_no_img, ImageView.ScaleType.CENTER_INSIDE);
            loader.placeHolder(
                    R.drawable.preview_img, ImageView.ScaleType.CENTER);

            ImageLoadInfo imageInfo = getImageInfo(extraInfo);
            layoutParams.width = imageInfo.width;
            layoutParams.height = imageInfo.height;
            ivFileImage.setLayoutParams(layoutParams);

            if (imageInfo.needCrop) {
                loader.actualImageScaleType(ImageView.ScaleType.CENTER_INSIDE);
            } else {
                loader.actualImageScaleType(ImageView.ScaleType.FIT_CENTER);
            }

            int backgroundColor = resources.getColor(R.color.jandi_messages_image_view_bg);
            vgFileImageWrapper.setBackgroundColor(backgroundColor);

            Uri uri = isFromLocalFilePath
                    ? UriUtil.getFileUri(localFilePath) : Uri.parse(remoteFilePath);

            loader.listener(new SimpleRequestListener<Uri, GlideDrawable>() {
                @Override
                public boolean onException(Exception e,
                                           Uri model, Target<GlideDrawable> target,
                                           boolean isFirstResource) {
                    vgFileImageWrapper.setBackgroundColor(bigSizeImageBackgroundColor);
                    return false;
                }
            });

            loader.uri(uri).into(ivFileImage);
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

            height = height > maxImageHeight ? maxImageHeight : height;
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

    private void setImageViewSizeToDefault() {
        int height = ivFileImage.getResources()
                .getDimensionPixelSize(R.dimen.jandi_messages_image_height);
        ViewGroup.LayoutParams layoutParams = ivFileImage.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = height;
        ivFileImage.setLayoutParams(layoutParams);
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
