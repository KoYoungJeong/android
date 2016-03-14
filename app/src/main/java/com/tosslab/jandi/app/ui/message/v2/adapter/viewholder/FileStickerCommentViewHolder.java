package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 6. 9..
 */
public class FileStickerCommentViewHolder implements BodyViewHolder {
    private SimpleDraweeView ivProfile;
    private TextView tvName;
    private TextView tvDate;
    private TextView tvFileOwner;
    private TextView tvFileName;
    private SimpleDraweeView ivSticker;
    private SimpleDraweeView ivFileImage;
    private View vDisableCover;
    private View vDisableLineThrough;
    private TextView tvUnread;
    private Context context;
    private View vLastRead;
    private View contentView;
    private View vFileImageRound;

    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_message_item);
        ivProfile = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_user_profile);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
        tvDate = (TextView) rootView.findViewById(R.id.tv_message_create_date);

        tvFileOwner = (TextView) rootView.findViewById(R.id.tv_message_commented_owner);
        tvFileName = (TextView) rootView.findViewById(R.id.tv_message_commented_file_name);
        ivSticker = (SimpleDraweeView) rootView.findViewById(R.id.iv_sticker_message_commented_content);

        ivFileImage = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_commented_photo);
        vFileImageRound = rootView.findViewById(R.id.iv_message_commented_photo_round);

        vDisableCover = rootView.findViewById(R.id.v_entity_listitem_warning);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);

        tvUnread = (TextView) rootView.findViewById(R.id.tv_entity_listitem_unread);
        context = rootView.getContext();
        vLastRead = rootView.findViewById(R.id.vg_message_last_read);
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {

        long fromEntityId = link.fromEntity;

        FormattedEntity entity = EntityManager.getInstance().getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = entity.getUserLargeProfileUrl();

        ImageUtil.loadProfileImage(ivProfile, profileUrl, R.drawable.profile_img);

        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
        ResLeftSideMenu.User user = entityById != EntityManager.UNKNOWN_USER_ENTITY ? entityById.getUser() : null;

        FormattedEntity feedbackEntityById = entityManager.getEntityById(link.feedback.writerId);
        ResLeftSideMenu.User feedbackUser = feedbackEntityById != EntityManager.UNKNOWN_USER_ENTITY ? feedbackEntityById.getUser() : null;

        if (user != null && TextUtils.equals(user.status, "enabled")) {
            tvName.setTextColor(context.getResources().getColor(R.color.jandi_messages_name));
            vDisableCover.setVisibility(View.GONE);
            vDisableLineThrough.setVisibility(View.GONE);
        } else {
            tvName.setTextColor(
                    context.getResources().getColor(R.color.deactivate_text_color));
            vDisableCover.setVisibility(View.VISIBLE);
            vDisableLineThrough.setVisibility(View.VISIBLE);
        }

        int unreadCount = UnreadCountUtil.getUnreadCount(
                teamId, roomId, link.id, fromEntityId, entityManager.getMe().getId());

        tvUnread.setText(String.valueOf(unreadCount));
        if (unreadCount <= 0) {
            tvUnread.setVisibility(View.GONE);
        } else {
            tvUnread.setVisibility(View.VISIBLE);
        }

        tvName.setText(fromEntity.name);

        tvDate.setText(DateTransformator.getTimeStringForSimple(link.time));

        if (link.message instanceof ResMessages.CommentStickerMessage) {
            ResMessages.CommentStickerMessage commentSticker =
                    (ResMessages.CommentStickerMessage) link.message;
            ResMessages.StickerContent content = commentSticker.content;

            StickerManager.getInstance()
                    .loadStickerNoOption(ivSticker, content.groupId, content.stickerId);
        }

        ivProfile.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Image)));
        tvName.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Name)));

        if (link.feedback instanceof ResMessages.FileMessage) {
            ResMessages.FileMessage feedbackFileMessage = link.feedback;

            Resources resources = tvFileName.getResources();
            vFileImageRound.setVisibility(View.GONE);
            if (TextUtils.equals(link.feedback.status, "archived")) {
                tvFileOwner.setVisibility(View.GONE);

                tvFileName.setText(R.string.jandi_deleted_file);
                tvFileName.setTextColor(resources.getColor(R.color.jandi_text_light));

                ImageLoader.newBuilder()
                        .actualScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                        .load(R.drawable.file_icon_deleted)
                        .into(ivFileImage);

                ivFileImage.setOnClickListener(null);
            } else {
                Spanned fileOwner = Html.fromHtml(
                        resources.getString(R.string.jandi_commented_on, feedbackUser.name));
                tvFileOwner.setText(fileOwner);
                tvFileOwner.setVisibility(View.VISIBLE);

                tvFileName.setTextColor(resources.getColor(R.color.jandi_messages_file_name));

                ResMessages.FileContent content = feedbackFileMessage.content;
                tvFileName.setText(content.title);

                String serverUrl = content.serverUrl;
                String fileType = content.icon;
                String fileUrl = content.fileUrl;
                String thumbnailUrl =
                        ImageUtil.getThumbnailUrl(content.extraInfo, ImageUtil.Thumbnails.SMALL);
                ImageUtil.setResourceIconOrLoadImage(
                        ivFileImage, vFileImageRound,
                        fileUrl, thumbnailUrl,
                        serverUrl, fileType);

                MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(serverUrl);

                if (MimeTypeUtil.isFileFromGoogleOrDropbox(sourceType)) {
                    ivFileImage.setOnClickListener(v -> {
                        String imageUrl =
                                ImageUtil.getThumbnailUrlOrOriginal(
                                        content, ImageUtil.Thumbnails.ORIGINAL);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
                        context.startActivity(intent);

                    });
                } else {
                    ivFileImage.setOnClickListener(null);
                }
            }
        }
    }

    @Override
    public void setLastReadViewVisible(long currentLinkId, long lastReadLinkId) {
        if (currentLinkId == lastReadLinkId) {
            vLastRead.setVisibility(View.VISIBLE);
        } else {
            vLastRead.setVisibility(View.GONE);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_sticker_cmt_with_file_v2;
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
}
