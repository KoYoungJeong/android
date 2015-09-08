package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.sticker.StickerManager;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.transform.ion.IonCircleTransform;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 6. 9..
 */
public class FileStickerCommentViewHolder implements BodyViewHolder {
    private ImageView ivProfile;
    private TextView tvName;
    private TextView tvDate;
    private TextView tvFileOwner;
    private TextView tvFileName;
    private ImageView ivSticker;
    private TextView tvFileOwnerPostfix;
    private ImageView ivFileImage;
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
        ivProfile = (ImageView) rootView.findViewById(R.id.iv_message_user_profile);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
        tvDate = (TextView) rootView.findViewById(R.id.tv_message_create_date);

        tvFileOwner = (TextView) rootView.findViewById(R.id.tv_message_commented_owner);
        tvFileOwnerPostfix = (TextView) rootView.findViewById(R.id.tv_message_commented_postfix);
        tvFileName = (TextView) rootView.findViewById(R.id.tv_message_commented_file_name);
        ivSticker = (ImageView) rootView.findViewById(R.id.iv_sticker_message_commented_content);

        ivFileImage = (ImageView) rootView.findViewById(R.id.iv_message_commented_photo);
        vFileImageRound = rootView.findViewById(R.id.iv_message_commented_photo_round);

        vDisableCover = rootView.findViewById(R.id.v_entity_listitem_warning);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);

        tvUnread = (TextView) rootView.findViewById(R.id.tv_entity_listitem_unread);
        context = rootView.getContext();
        vLastRead = rootView.findViewById(R.id.vg_message_last_read);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {

        int fromEntityId = link.fromEntity;

        FormattedEntity entity = EntityManager.getInstance().getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = entity.getUserLargeProfileUrl();

        LogUtil.e("profileUrl - " + profileUrl);

        Ion.with(ivProfile)
                .placeholder(R.drawable.profile_img)
                .error(R.drawable.profile_img)
                .transform(new IonCircleTransform())
                .crossfade(true)
                .load(profileUrl);

        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
        ResLeftSideMenu.User user = entityById != null ? entityById.getUser() : null;

        FormattedEntity feedbackEntityById = entityManager.getEntityById(link.feedback.writerId);
        ResLeftSideMenu.User feedbackUser = feedbackEntityById != null ? feedbackEntityById.getUser() : null;

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

        if (link.feedback instanceof ResMessages.FileMessage) {
            vFileImageRound.setVisibility(View.GONE);

            ResMessages.FileMessage feedbackFileMessage = link.feedback;
            ivFileImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            if (TextUtils.equals(link.feedback.status, "archived")) {
                tvFileOwner.setVisibility(View.GONE);
                tvFileOwnerPostfix.setVisibility(View.GONE);

                tvFileName.setText(R.string.jandi_deleted_file);
                tvFileName.setTextColor(tvFileName.getResources().getColor(R.color
                        .jandi_text_light));
                ivFileImage.setBackgroundDrawable(null);
                ivFileImage.setVisibility(View.VISIBLE);
                ivFileImage.setOnClickListener(null);
            } else {
                tvFileOwner.setText(feedbackUser.name);
                tvFileName.setTextColor(tvFileName.getResources().getColor(R.color
                        .jandi_messages_file_name));
                ResMessages.FileContent content = feedbackFileMessage.content;
                tvFileName.setText(content.title);

                tvFileOwner.setVisibility(View.VISIBLE);
                tvFileOwnerPostfix.setVisibility(View.VISIBLE);

                String fileType = content.type;
                if (fileType.startsWith("image/")) {
                    if (BitmapUtil.hasImageUrl(content)) {
                        String thumbnailUrl = BitmapUtil.getThumbnailUrlOrOriginal(
                                content, BitmapUtil.Thumbnails.SMALL);
                        MimeTypeUtil.SourceType sourceType =
                                SourceTypeUtil.getSourceType(content.serverUrl);
                        switch (sourceType) {
                            case Google:
                            case Dropbox:
                                int mimeTypeIconImage =
                                        MimeTypeUtil.getMimeTypeIconImage(
                                                content.serverUrl, content.icon);
                                ivFileImage.setBackgroundDrawable(null);
                                ivFileImage.setImageResource(mimeTypeIconImage);
                                ivFileImage.setOnClickListener(view -> {
                                    Intent intent = new Intent(Intent.ACTION_VIEW,
                                            Uri.parse(
                                                    BitmapUtil.getThumbnailUrlOrOriginal(
                                                            content, BitmapUtil.Thumbnails.ORIGINAL)));
                                    context.startActivity(intent);
                                });
                                break;
                            default:
                                vFileImageRound.setVisibility(View.VISIBLE);
                                Ion.with(ivFileImage)
                                        .placeholder(R.drawable.file_icon_img)
                                        .error(R.drawable.file_icon_img)
                                        .crossfade(true)
                                        .centerCrop()
                                        .load(thumbnailUrl);

                                break;
                        }

                    } else {
                        ivFileImage.setBackgroundDrawable(null);
                        ivFileImage.setImageResource(
                                MimeTypeUtil.getMimeTypeIconImage(content.serverUrl, content.icon));
                    }
                } else {
                    ivFileImage.setBackgroundDrawable(null);
                    ivFileImage.setImageResource(
                            MimeTypeUtil.getMimeTypeIconImage(content.serverUrl, content.icon));
                }
            }

        }

        if (link.message instanceof ResMessages.CommentStickerMessage) {
            ResMessages.CommentStickerMessage commentSticker =
                    (ResMessages.CommentStickerMessage) link.message;
            ResMessages.StickerContent content = commentSticker.content;

            StickerManager.getInstance()
                    .loadStickerNoOption(ivSticker, content.groupId, content.stickerId);
        }

        final ShowProfileEvent event = new ShowProfileEvent(fromEntity.id);
        ivProfile.setOnClickListener(v -> EventBus.getDefault().post(event));
        tvName.setOnClickListener(v -> EventBus.getDefault().post(event));
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
