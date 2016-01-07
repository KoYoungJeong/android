package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class ImageViewHolder implements BodyViewHolder {

    public static final String TAG = ImageViewHolder.class.getSimpleName();
    public static final int MAX_WIDTH_WHEN_VERTICAL_IMAGE = 160;
    public static final int MAX_HEIGHT_WHEN_VERTICAL_IMAGE = 284;

    public static final int MAX_WIDTH = 213;
    public static final int MAX_HEIGHT = 120;

    public static final int MIN_SIZE = 46;
    public static final int SMALL_SIZE = 90;

    private static final float LONG_HORIZONTAL_RATIO = 46 / 213f;
    private static final float LONG_VERTICAL_RATIO = 284 / 46f;

    private SimpleDraweeView ivProfile;
    private TextView tvName;
    private TextView tvDate;
    private SimpleDraweeView ivFileImage;
    private TextView tvFileName;
    private TextView tvFileType;
    private TextView tvUploader;
    private View vDisableCover;
    private View vDisableLineThrough;
    private TextView tvUnread;
    private Context context;
    private View vLastRead;
    private View contentView;

    private int smallSizePixel;
    private int maxWidthPixelWhenVerticalImage;
    private int maxHeightPixelWhenVerticalImage;
    private int minimumSizePixel;
    private int maxWidthPixel;
    private int maxHeightPixel;
    private int cornerRadiusPixel;

    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_message_item);
        ivProfile = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_user_profile);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
        tvDate = (TextView) rootView.findViewById(R.id.tv_message_create_date);

        ivFileImage = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_photo);
        tvFileName = (TextView) rootView.findViewById(R.id.tv_message_image_file_name);
        tvFileType = (TextView) rootView.findViewById(R.id.tv_img_file_type);
        tvUploader = (TextView) rootView.findViewById(R.id.tv_img_file_uploader);
        vDisableCover = rootView.findViewById(R.id.v_entity_listitem_warning);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);

        tvUnread = (TextView) rootView.findViewById(R.id.tv_entity_listitem_unread);
        vLastRead = rootView.findViewById(R.id.vg_message_last_read);

        context = rootView.getContext();

        initViewSizes();
    }

    // 계속 계산하지 않도록
    private void initViewSizes() {
        smallSizePixel = getPixelFromDp(SMALL_SIZE);
        minimumSizePixel = getPixelFromDp(MIN_SIZE);
        maxWidthPixelWhenVerticalImage = getPixelFromDp(MAX_WIDTH_WHEN_VERTICAL_IMAGE);
        maxHeightPixelWhenVerticalImage = getPixelFromDp(MAX_HEIGHT_WHEN_VERTICAL_IMAGE);
        maxWidthPixel = getPixelFromDp(MAX_WIDTH);
        maxHeightPixel = getPixelFromDp(MAX_HEIGHT);

        cornerRadiusPixel = getPixelFromDp(2);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {
        int fromEntityId = link.fromEntity;

        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity fromEntity = entityManager.getEntityById(fromEntityId);

        boolean isUnknownUser = fromEntity == EntityManager.UNKNOWN_USER_ENTITY;
        ResLeftSideMenu.User user = isUnknownUser ? null : fromEntity.getUser();

        bindUser(user, fromEntity.getUserLargeProfileUrl());

        bindUnreadCount(link.id, teamId, roomId, fromEntityId, entityManager.getMe().getId());

        tvDate.setText(DateTransformator.getTimeStringForSimple(link.time));

        if (!(link.message instanceof ResMessages.FileMessage)) {
            return;
        }

        ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) link.message;
        ResMessages.FileContent fileContent = fileMessage.content;
        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(fileContent.serverUrl);

        bindUploader(user, fileMessage.writerId);

        bindFileImage(fileMessage, fileContent, sourceType);
    }

    private void bindUser(ResLeftSideMenu.User user, String userProfileUrl) {
        ImageUtil.loadProfileImage(ivProfile, userProfileUrl, R.drawable.profile_img);

        tvName.setText(user != null ? user.name : "");

        if (user != null && TextUtils.equals(user.status, "enabled")) {
            tvName.setTextColor(context.getResources().getColor(R.color.jandi_messages_name));
            tvName.setText(user.name);
            vDisableCover.setVisibility(View.GONE);
            vDisableLineThrough.setVisibility(View.GONE);

            int userId = user.id;
            ShowProfileEvent eventFromImage = new ShowProfileEvent(userId, ShowProfileEvent.From.Image);
            ivProfile.setOnClickListener(v -> EventBus.getDefault().post(eventFromImage));

            ShowProfileEvent eventFromName = new ShowProfileEvent(userId, ShowProfileEvent.From.Name);
            tvName.setOnClickListener(v -> EventBus.getDefault().post(eventFromName));
        } else {
            tvName.setTextColor(
                    context.getResources().getColor(R.color.deactivate_text_color));
            vDisableCover.setVisibility(View.VISIBLE);
            vDisableLineThrough.setVisibility(View.VISIBLE);

            ivProfile.setOnClickListener(null);
            tvName.setOnClickListener(null);
        }
    }

    private void bindUnreadCount(int linkId, int teamId, int roomId, int fromEntityId, int myId) {
        int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId, linkId, fromEntityId, myId);
        tvUnread.setText(String.valueOf(unreadCount));
        if (unreadCount <= 0) {
            tvUnread.setVisibility(View.GONE);
        } else {
            tvUnread.setVisibility(View.VISIBLE);
        }
    }

    private void bindUploader(ResLeftSideMenu.User user, int writerId) {
        if (isWriter(user, writerId)) {
            tvUploader.setVisibility(View.GONE);
        } else {
            tvUploader.setVisibility(View.VISIBLE);
            String shared = tvUploader.getContext().getString(R.string.jandi_shared);
            String name = EntityManager.getInstance().getEntityById(writerId).getName();
            String ofFile = tvUploader.getContext().getString(R.string.jandi_who_of_file);

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(shared).append(" ");
            int startIdx = builder.length();
            builder.append(name);
            int lastIdx = builder.length();
            builder.append(ofFile);

            int textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11f, tvUploader
                    .getResources().getDisplayMetrics());

            builder.setSpan(new NameSpannable(textSize, Color.BLACK),
                    startIdx,
                    lastIdx,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            tvUploader.setText(builder);
        }
    }

    private boolean isWriter(ResLeftSideMenu.User user, int writerId) {
        return user != null && user.id == writerId;
    }

    private void bindFileImage(ResMessages.FileMessage fileMessage,
                               ResMessages.FileContent fileContent, MimeTypeUtil.SourceType sourceType) {

        if (TextUtils.equals(fileMessage.status, "archived")) {
            tvFileName.setText(R.string.jandi_deleted_file);
            ivFileImage.setImageURI(UriFactory.getResourceUri(R.drawable.jandi_fview_icon_deleted));
            tvFileType.setText("");
            return;
        }

        tvFileName.setText(fileContent.title);

        if (!ImageUtil.hasImageUrl(fileContent)) {
            ImageLoader.newBuilder()
                    .actualScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                    .load(R.drawable.file_icon_img)
                    .into(ivFileImage);
            return;
        }

        // Google, Dropbox 파일이 인 경우
        if (isFileFromGoogleOrDropbox(sourceType)) {
            String serverUrl = fileContent.serverUrl;
            String icon = fileContent.icon;
            int mimeTypeIconImage = MimeTypeUtil.getMimeTypeIconImage(serverUrl, icon);
            ivFileImage.setImageURI(UriFactory.getResourceUri(mimeTypeIconImage));
            tvFileType.setText(fileContent.ext);
        } else {
            String fileSize = FileUtil.fileSizeCalculation(fileContent.size);
            tvFileType.setText(String.format("%s, %s", fileSize, fileContent.ext));

            String localFilePath = ImageUtil.getLocalFilePath(fileMessage.id);
            boolean isFromLocalFilePath = !TextUtils.isEmpty(localFilePath);

            final ResMessages.ThumbnailUrls extraInfo = fileContent.extraInfo;
            String remoteFilePth =
                    ImageUtil.getThumbnailUrl(extraInfo, ImageUtil.Thumbnails.LARGE);

            final ViewGroup.LayoutParams layoutParams = ivFileImage.getLayoutParams();

            ImageLoader.Builder imageRequestBuilder = ImageLoader.newBuilder();
            imageRequestBuilder.error(R.drawable.image_no_preview, ScalingUtils.ScaleType.FIT_XY);

            // 유효한 확장자가 아닌 경우, Local File Path 도 없고 Thumbnail Path 도 없는 경우
            boolean shouldSupportImageExtensions =
                    FileExtensionsUtil.shouldSupportImageExtensions(fileContent.ext);
            if (!shouldSupportImageExtensions
                    || (!isFromLocalFilePath && TextUtils.isEmpty(remoteFilePth))) {
                LogUtil.i(TAG, "Thumbnail's are empty.");
                layoutParams.width = smallSizePixel;
                layoutParams.height = smallSizePixel;

                ivFileImage.setLayoutParams(layoutParams);
                ivFileImage.requestLayout();

                imageRequestBuilder.actualScaleType(ScalingUtils.ScaleType.FIT_XY);

                imageRequestBuilder.load(R.drawable.image_no_preview)
                        .into(ivFileImage);
                return;
            }

            imageRequestBuilder.placeHolder(
                    R.drawable.comment_image_preview_download, ScalingUtils.ScaleType.FIT_XY);

            boolean needToResize = true;

            int width;
            int height;

            if (extraInfo != null && extraInfo.width > 0 && extraInfo.height > 0) {
                ImageSpec imageSpec = getImageSpec(
                        extraInfo.width, extraInfo.height, extraInfo.orientation);
                switch (imageSpec.getType()) {
                    case SMALL:
                        imageRequestBuilder.actualScaleType(ScalingUtils.ScaleType.FIT_CENTER);
                        needToResize = false;
                        break;
                    default:
                        imageRequestBuilder.actualScaleType(ScalingUtils.ScaleType.CENTER_CROP);
                        RoundingParams roundingParams =
                                RoundingParams.fromCornersRadius(cornerRadiusPixel);
                        imageRequestBuilder.roundingParams(roundingParams);
                        break;
                }
                width = imageSpec.getWidth();
                height = imageSpec.getHeight();
            } else {
                imageRequestBuilder.actualScaleType(ScalingUtils.ScaleType.CENTER_CROP);
                RoundingParams roundingParams =
                        RoundingParams.fromCornersRadius(cornerRadiusPixel);
                imageRequestBuilder.roundingParams(roundingParams);

                width = maxWidthPixel;
                height = maxHeightPixel;
            }

            layoutParams.width = width;
            layoutParams.height = height;
            ivFileImage.setLayoutParams(layoutParams);

            Uri uri = isFromLocalFilePath
                    ? UriFactory.getFileUri(localFilePath) : Uri.parse(remoteFilePth);

            if (needToResize) {
                imageRequestBuilder.resize(width, height);
            }

            imageRequestBuilder.load(uri)
                    .into(ivFileImage);
        }
    }

    private ImageSpec getImageSpec(int width, int height, int orientation) {
        // Vertical Image.
        if (ImageUtil.isVerticalPhoto(orientation)) {
            int temp = height;
            height = width;
            width = temp;
        }

        float ratio = height / (float) width;
        LogUtil.i(TAG, String.format("%d, %d, %d, %f", width, height, orientation, ratio));

        if (isSmallSize(width, height)) {
            ImageSpec.Type type = ImageSpec.Type.SMALL;
            int size = smallSizePixel;
            return new ImageSpec(size, size, orientation, type);
        }

        if (width == height) {
            ImageSpec.Type type = ImageSpec.Type.SQUARE;
            int size = Math.min(width, maxWidthPixelWhenVerticalImage);
            return new ImageSpec(size, size, orientation, type);
        }

        if (width > height) {
            width = Math.min(width, maxWidthPixel);

            if (ratio <= LONG_HORIZONTAL_RATIO) {
                ImageSpec.Type type = ImageSpec.Type.LONG_HORIZONTAL;
                height = minimumSizePixel;
                return new ImageSpec(width, height, orientation, type);
            }

            ImageSpec.Type type = ImageSpec.Type.HORIZONTAL;
            height = (int) (width * ratio);
            return new ImageSpec(width, height, orientation, type);
        } else {
            height = Math.min(height, maxHeightPixelWhenVerticalImage);

            if (ratio > LONG_VERTICAL_RATIO) {
                ImageSpec.Type type = ImageSpec.Type.LONG_VERTICAL;
                width = minimumSizePixel;
                return new ImageSpec(width, height, orientation, type);
            }

            ImageSpec.Type type = ImageSpec.Type.VERTICAL;
            width = Math.min((int) (height / ratio), maxWidthPixelWhenVerticalImage);
            return new ImageSpec(width, height, orientation, type);
        }
    }

    private boolean isSmallSize(int width, int height) {
        final int smallSizeXXHDPI = SMALL_SIZE * 3;
        return width <= smallSizeXXHDPI && height <= smallSizeXXHDPI;
    }

    private int getPixelFromDp(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    private boolean isFileFromGoogleOrDropbox(MimeTypeUtil.SourceType sourceType) {
        return sourceType == MimeTypeUtil.SourceType.Google
                || sourceType == MimeTypeUtil.SourceType.Dropbox;
    }

    @Override
    public void setLastReadViewVisible(int currentLinkId, int lastReadLinkId) {
        if (currentLinkId == lastReadLinkId) {
            vLastRead.setVisibility(View.VISIBLE);
        } else {
            vLastRead.setVisibility(View.GONE);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_img_v2;
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        if (contentView != null && itemClickListener != null) {
            contentView.setOnClickListener(itemClickListener);
        }
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        if (contentView != null && itemLongClickListener != null) {
            contentView.setOnLongClickListener(itemLongClickListener);
        }
    }

    private static class ImageSpec {
        private int width;
        private int height;
        private int orientation;
        private Type type = Type.MAX;

        public ImageSpec(int width, int height, int orientation, Type type) {
            this.width = width;
            this.height = height;
            this.orientation = orientation;
            this.type = type;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getOrientation() {
            return orientation;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "ImageSpec{" +
                    "width=" + width +
                    ", height=" + height +
                    ", orientation=" + orientation +
                    ", type=" + type +
                    '}';
        }

        public enum Type {
            MAX, HORIZONTAL, VERTICAL, LONG_HORIZONTAL, LONG_VERTICAL, SQUARE, SMALL
        }
    }

}
