package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

import java.util.ArrayList;
import java.util.Collection;

import de.greenrobot.event.EventBus;

/**
 * Created by tee on 16. 4. 7..
 */
public class StickerCommentViewHolder extends BaseCommentViewHolder {

    private ViewGroup vgMessageCommonFile;
    private SimpleDraweeView ivMessageCommonFile;
    private TextView tvCommonFileOwner;
    private TextView tvMessageBadge;
    private TextView tvMessageTime;

    private SimpleDraweeView ivProfileNestedUserProfileForSticker;
    private TextView tvProfileNestedUserNameForSticker;
    private ImageView ivProfileNestedLineThroughForSticker;

    private SimpleDraweeView ivProfileNestedCommentSticker;
    private TextView tvProfileNestedCommentStickerCreateDate;
    private TextView tvProfileNestedCommentStickerUnread;
    private TextView tvMessageCommonFileName;
    private Context context;
    private boolean hasNestedProfile = false;
    private boolean hasOnlyBadge;
    private TextView tvFileUploaderName;
    private TextView tvCommonFileSize;

    private StickerCommentViewHolder() {
    }

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        // 파일 정보
        vgMessageCommonFile = (ViewGroup) rootView.findViewById(R.id.vg_message_common_file);
        ivMessageCommonFile = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_common_file);
        tvMessageCommonFileName = (TextView) rootView.findViewById(R.id.tv_message_common_file_name);
        tvFileUploaderName = (TextView) rootView.findViewById(R.id.tv_uploader_name);
        tvCommonFileSize = (TextView) rootView.findViewById(R.id.tv_common_file_size);
        tvMessageBadge = (TextView) rootView.findViewById(R.id.tv_message_badge);
        tvMessageTime = (TextView) rootView.findViewById(R.id.tv_message_time);

        tvMessageBadge.setVisibility(View.GONE);
        tvMessageTime.setVisibility(View.GONE);

        // 커멘트 스티커 프로필
        ivProfileNestedUserProfileForSticker = (SimpleDraweeView) rootView.findViewById(R.id.iv_profile_nested_user_profile_for_sticker);
        tvProfileNestedUserNameForSticker = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_user_name_for_sticker);
        ivProfileNestedLineThroughForSticker = (ImageView) rootView.findViewById(R.id.iv_profile_nested_name_line_through_for_sticker);

        // 스티커
        ivProfileNestedCommentSticker = (SimpleDraweeView) rootView.findViewById(R.id.iv_profile_nested_comment_sticker);
        tvProfileNestedCommentStickerCreateDate = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_sticker_create_date);
        tvProfileNestedCommentStickerUnread = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_sticker_unread);
        context = rootView.getContext();

        if (hasNestedProfile) {
            ivProfileNestedUserProfileForSticker.setVisibility(View.VISIBLE);
            tvProfileNestedUserNameForSticker.setVisibility(View.VISIBLE);
        } else {
            ivProfileNestedUserProfileForSticker.setVisibility(View.INVISIBLE);
            tvProfileNestedUserNameForSticker.setVisibility(View.GONE);
        }

    }

    @Override
    protected void initObjects() {
        vgProfileNestedComment.setVisibility(View.GONE);
        vgProfileNestedCommentSticker.setVisibility(View.VISIBLE);
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        super.bindData(link, teamId, roomId, entityId);
        if (hasFileInfoView()) {
            settingFileInfo(link, roomId);
        }
        settingCommentUserInfo(link);
        getStickerComment(link, teamId, roomId);
    }

    private void getStickerComment(ResMessages.Link link, long teamId, long roomId) {
        ResMessages.CommentStickerMessage message = (ResMessages.CommentStickerMessage) link.message;

        StickerManager.getInstance().loadStickerNoOption(ivProfileNestedCommentSticker, message.content.groupId, message.content.stickerId);

        if (hasOnlyBadge) {
            tvProfileNestedCommentStickerCreateDate.setVisibility(View.GONE);
        } else {
            tvProfileNestedCommentStickerCreateDate.setVisibility(View.VISIBLE);
            tvProfileNestedCommentStickerCreateDate.setText(DateTransformator.getTimeStringForSimple(message.createTime));

        }
        int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
                link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());
        if (unreadCount > 0) {
            tvProfileNestedCommentStickerUnread.setText(String.valueOf(unreadCount));
            tvProfileNestedCommentStickerUnread.setVisibility(View.VISIBLE);
        } else {
            tvProfileNestedCommentStickerUnread.setVisibility(View.GONE);
        }
    }

    private void settingCommentUserInfo(ResMessages.Link link) {
        long fromEntityId = link.fromEntity;
        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entity = entityManager.getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = entity.getUserLargeProfileUrl();

        ImageUtil.loadProfileImage(
                ivProfileNestedUserProfileForSticker, profileUrl, R.drawable.profile_img);

        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
        ResLeftSideMenu.User user = entityById != EntityManager.UNKNOWN_USER_ENTITY ? entityById.getUser() : null;

        if (user != null && entityById.isEnabled()) {
            tvProfileNestedUserNameForSticker.setTextColor(
                    JandiApplication.getContext().getResources().getColor(R.color.jandi_messages_name));
            ivProfileNestedLineThroughForSticker.setVisibility(View.GONE);
        } else {
            tvProfileNestedUserNameForSticker.setTextColor(
                    JandiApplication.getContext().getResources().getColor(R.color.deactivate_text_color));
            ivProfileNestedLineThroughForSticker.setVisibility(View.VISIBLE);
        }

        tvProfileNestedUserNameForSticker.setText(fromEntity.name);

        ivProfileNestedUserProfileForSticker.setOnClickListener(
                v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Image)));

        tvProfileNestedUserNameForSticker.setOnClickListener(
                v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Name)));
    }

    private void settingFileInfo(ResMessages.Link link, long roomId) {
        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity room = entityManager.getEntityById(roomId);
        boolean isPublicTopic = room.isPublicTopic();

        FormattedEntity feedbackEntityById =
                entityManager.getEntityById(link.feedback.writerId);
        ResLeftSideMenu.User feedbackUser =
                feedbackEntityById != EntityManager.UNKNOWN_USER_ENTITY ? feedbackEntityById.getUser() : null;

        tvFileUploaderName.setText(feedbackUser.name);

        ResMessages.FileContent fileContent = link.feedback.content;

        String fileSize = FileUtil.fileSizeCalculation(fileContent.size);

        tvCommonFileSize.setText(fileSize);

        tvMessageTime.setText(DateTransformator.getTimeStringForSimple(link.time));

        if (link.feedback instanceof ResMessages.FileMessage) {

            ResMessages.FileMessage feedbackFileMessage = link.feedback;

            boolean isSharedFile = false;

            Collection<ResMessages.OriginalMessage.IntegerWrapper> shareEntities = feedbackFileMessage.shareEntities;

            // ArrayList로 나오는 경우 아직 DB에 기록되지 않은 경우 - object가 자동갱신되지 않는 문제 해결
            if (shareEntities instanceof ArrayList) {
                ResMessages.FileMessage file = MessageRepository.getRepository().getFileMessage(feedbackFileMessage.id);
                if (file != null && file.shareEntities != null) {
                    shareEntities = file.shareEntities;
                }
            }

            if (shareEntities != null) {
                for (ResMessages.OriginalMessage.IntegerWrapper e : shareEntities) {
                    if (e.getShareEntity() == roomId) {
                        isSharedFile = true;
                    }
                }
            }

            tvMessageCommonFileName.setTypeface(null, Typeface.BOLD);

            final Resources resources = tvMessageCommonFileName.getResources();
            if (TextUtils.equals(link.feedback.status, "archived")) {
                tvMessageCommonFileName.setText(R.string.jandi_deleted_file);
                tvMessageCommonFileName.setTextColor(resources.getColor(R.color.jandi_text_light));

                ImageLoader.newBuilder()
                        .actualScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                        .load(R.drawable.file_icon_deleted)
                        .into(ivMessageCommonFile);

                ivMessageCommonFile.setOnClickListener(null);
            } else {
                final ResMessages.FileContent content = feedbackFileMessage.content;

                if (!isSharedFile) {
                    tvMessageCommonFileName.setTypeface(null, Typeface.NORMAL);
                    int testSizePx = resources.getDimensionPixelSize(R.dimen.jandi_text_size_11sp);
                    tvMessageCommonFileName.setTextSize(TypedValue.COMPLEX_UNIT_PX, testSizePx);
                    SpannableStringBuilder unshareTextBuilder = new SpannableStringBuilder();
                    String title = content.title;
                    if (content.title.length() > 15) {
                        unshareTextBuilder.append(title.substring(0, 14))
                                .append("...");
                    } else {
                        unshareTextBuilder.append(title).append(" ");
                    }

                    unshareTextBuilder.setSpan(
                            new StyleSpan(Typeface.BOLD),
                            0, unshareTextBuilder.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    unshareTextBuilder.append(resources.getString(R.string.jandi_unshared_file));
                    tvMessageCommonFileName.setText(unshareTextBuilder);
                    tvMessageCommonFileName.setTextSize(TypedValue.COMPLEX_UNIT_PX, testSizePx);
                    tvMessageCommonFileName.setTextColor(resources.getColor(R.color.jandi_text_light));

                    ivMessageCommonFile.setClickable(false);

                    int resId = R.drawable.file_icon_unshared;
                    if (isPublicTopic) {
                        resId = MimeTypeUtil.getMimeTypeIconImage(content.serverUrl, content.icon);
                    }

                    ImageLoader.newBuilder()
                            .actualScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                            .load(resId)
                            .into(ivMessageCommonFile);
                } else {
                    tvMessageCommonFileName.setText(content.title);
                    tvMessageCommonFileName.setTextColor(resources.getColor(R.color.jandi_messages_file_name));

                    String serverUrl = content.serverUrl;
                    String fileType = content.icon;
                    String fileUrl = content.fileUrl;
                    String thumbnailUrl =
                            ImageUtil.getThumbnailUrl(content.extraInfo, ImageUtil.Thumbnails.SMALL);
                    ImageUtil.setResourceIconOrLoadImage(
                            ivMessageCommonFile, null,
                            fileUrl, thumbnailUrl,
                            serverUrl, fileType);

                    MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(serverUrl);
                    if (MimeTypeUtil.isFileFromGoogleOrDropbox(sourceType)) {
                        ivMessageCommonFile.setOnClickListener(v -> {
                            String imageUrl =
                                    ImageUtil.getThumbnailUrlOrOriginal(
                                            content, ImageUtil.Thumbnails.ORIGINAL);
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
                            context.startActivity(intent);
                        });
                    } else {
                        ivMessageCommonFile.setOnClickListener(null);
                    }
                }
            }
        }
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        super.setOnItemClickListener(itemClickListener);
        vgMessageCommonFile.setOnClickListener(itemClickListener);
        vgReadMore.setOnClickListener(itemClickListener);
        vgProfileNestedCommentSticker.setOnClickListener(itemClickListener);
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        super.setOnItemLongClickListener(itemLongClickListener);
        vgProfileNestedCommentSticker.setOnLongClickListener(itemLongClickListener);
    }

    private void setHasNestedProfile(boolean hasNestedProfile) {
        this.hasNestedProfile = hasNestedProfile;
    }

    public void setHasOnlyBadge(boolean hasOnlyBadge) {
        this.hasOnlyBadge = hasOnlyBadge;
    }

    public static class Builder extends BaseViewHolderBuilder {

        public StickerCommentViewHolder build() {
            StickerCommentViewHolder viewHolder = new StickerCommentViewHolder();
            viewHolder.setHasBottomMargin(hasBottomMargin);
            viewHolder.setHasSemiDivider(hasSemiDivider);
            viewHolder.setHasFileInfoView(hasFileInfoView);
            viewHolder.setHasCommentBubbleTail(hasCommentBubbleTail);
            viewHolder.setHasNestedProfile(hasNestedProfile);
            viewHolder.setHasViewAllComment(hasViewAllComment);
            viewHolder.setHasOnlyBadge(hasOnlyBadge);
            return viewHolder;
        }
    }

}
