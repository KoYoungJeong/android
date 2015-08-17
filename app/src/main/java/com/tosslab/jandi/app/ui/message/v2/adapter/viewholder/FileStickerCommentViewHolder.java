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
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.sticker.StickerManager;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 6. 9..
 */
public class FileStickerCommentViewHolder implements BodyViewHolder {
    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView dateTextView;
    private TextView fileOwnerTextView;
    private TextView fileNameTextView;
    private ImageView ivSticker;
    private TextView fileOwnerPostfixTextView;
    private ImageView fileImageView;
    private View disableCoverView;
    private View disableLineThroughView;
    private TextView unreadTextView;
    private Context context;
    private View lastReadView;
    private View contentView;

    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_message_item);
        profileImageView = (ImageView) rootView.findViewById(R.id.img_message_user_profile);
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_user_name);
        dateTextView = (TextView) rootView.findViewById(R.id.txt_message_create_date);

        fileOwnerTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_owner);
        fileOwnerPostfixTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_postfix);
        fileNameTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_file_name);
        ivSticker = (ImageView) rootView.findViewById(R.id.iv_sticker_message_commented_content);

        fileImageView = (ImageView) rootView.findViewById(R.id.img_message_commented_photo);

        disableCoverView = rootView.findViewById(R.id.view_entity_listitem_warning);
        disableLineThroughView = rootView.findViewById(R.id.img_entity_listitem_line_through);

        unreadTextView = (TextView) rootView.findViewById(R.id.txt_entity_listitem_unread);
        context = rootView.getContext();
        lastReadView = rootView.findViewById(R.id.vg_message_last_read);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {

        int fromEntityId = link.fromEntity;

        FormattedEntity entity = EntityManager.getInstance(context).getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = entity.getUserLargeProfileUrl();

        LogUtil.e("profileUrl - " + profileUrl);

        Ion.with(profileImageView)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new IonCircleTransform())
                .crossfade(true)
                .load(profileUrl);

        EntityManager entityManager = EntityManager.getInstance(context);
        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
        ResLeftSideMenu.User user = entityById != null ? entityById.getUser() : null;

        FormattedEntity feedbackEntityById = entityManager.getEntityById(link.feedback.writerId);
        ResLeftSideMenu.User feedbackUser = feedbackEntityById != null ? feedbackEntityById.getUser() : null;

        if (user != null && TextUtils.equals(user.status, "enabled")) {
            nameTextView.setTextColor(context.getResources().getColor(R.color.jandi_messages_name));
            disableCoverView.setVisibility(View.GONE);
            disableLineThroughView.setVisibility(View.GONE);
        } else {
            nameTextView.setTextColor(
                    context.getResources().getColor(R.color.deactivate_text_color));
            disableCoverView.setVisibility(View.VISIBLE);
            disableLineThroughView.setVisibility(View.VISIBLE);
        }

        int unreadCount = UnreadCountUtil.getUnreadCount(
                teamId, roomId, link.id, fromEntityId, entityManager.getMe().getId());

        unreadTextView.setText(String.valueOf(unreadCount));
        if (unreadCount <= 0) {
            unreadTextView.setVisibility(View.GONE);
        } else {
            unreadTextView.setVisibility(View.VISIBLE);
        }

        nameTextView.setText(fromEntity.name);

        dateTextView.setText(DateTransformator.getTimeStringForSimple(link.time));

        if (link.feedback instanceof ResMessages.FileMessage) {

            ResMessages.FileMessage feedbackFileMessage = (ResMessages.FileMessage) link.feedback;
            fileImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            if (TextUtils.equals(link.feedback.status, "archived")) {
                fileOwnerTextView.setVisibility(View.GONE);
                fileOwnerPostfixTextView.setVisibility(View.GONE);

                fileNameTextView.setText(R.string.jandi_deleted_file);
                fileImageView.setBackgroundDrawable(null);
                fileImageView.setVisibility(View.VISIBLE);
                fileImageView.setOnClickListener(null);
            } else {
                fileOwnerTextView.setText(feedbackUser.name);
                ResMessages.FileContent content = feedbackFileMessage.content;
                fileNameTextView.setText(content.title);

                fileOwnerTextView.setVisibility(View.VISIBLE);
                fileOwnerPostfixTextView.setVisibility(View.VISIBLE);

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
                                fileImageView.setBackgroundDrawable(null);
                                fileImageView.setImageResource(mimeTypeIconImage);
                                fileImageView.setOnClickListener(view -> {
                                    Intent intent = new Intent(Intent.ACTION_VIEW,
                                            Uri.parse(
                                                    BitmapUtil.getThumbnailUrlOrOriginal(
                                                            content, BitmapUtil.Thumbnails.ORIGINAL)));
                                    context.startActivity(intent);
                                });
                                break;
                            default:
                                fileImageView.setBackgroundResource(R.drawable.jandi_message_image_frame);
                                Ion.with(fileImageView)
                                        .placeholder(R.drawable.jandi_fl_icon_img)
                                        .error(R.drawable.jandi_fl_icon_img)
                                        .crossfade(true)
                                        .centerCrop()
                                        .load(thumbnailUrl);

                                break;
                        }

                    } else {
                        fileImageView.setBackgroundDrawable(null);
                        fileImageView.setImageResource(
                                MimeTypeUtil.getMimeTypeIconImage(content.serverUrl, content.icon));
                    }
                } else {
                    fileImageView.setBackgroundDrawable(null);
                    fileImageView.setImageResource(
                            MimeTypeUtil.getMimeTypeIconImage(content.serverUrl, content.icon));
                }
            }

        }

        if (link.message instanceof ResMessages.CommentStickerMessage) {
            ResMessages.CommentStickerMessage commentStickerMessage = (ResMessages.CommentStickerMessage) link.message;
            StickerManager.getInstance().loadStickerNoOption(ivSticker, commentStickerMessage.content.groupId, commentStickerMessage.content.stickerId);

        }

        profileImageView.setOnClickListener(v ->
                EventBus.getDefault().post(new RequestUserInfoEvent(fromEntity.id)));
        nameTextView.setOnClickListener(v ->
                EventBus.getDefault().post(new RequestUserInfoEvent(fromEntity.id)));
    }

    @Override
    public void setLastReadViewVisible(int currentLinkId, int lastReadLinkId) {
        if (currentLinkId == lastReadLinkId) {
            lastReadView.setVisibility(View.VISIBLE);
        } else {
            lastReadView.setVisibility(View.GONE);
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
