package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
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
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.GenerateMentionMessageUtil;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class FileCommentViewHolder implements BodyViewHolder {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView fileOwnerTextView;
    private TextView fileNameTextView;
    private TextView commentTextView;
    private TextView fileOwnerPostfixTextView;
    private ImageView fileImageView;
    private View disableCoverView;
    private View disableLineThroughView;
    private Context context;
    private View lastReadView;
    private View fileImageRound;
    private View contentView;

    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_message_item);
        profileImageView = (ImageView) rootView.findViewById(R.id.img_message_user_profile);
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_user_name);

        fileOwnerTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_owner);
        fileOwnerPostfixTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_postfix);
        fileNameTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_file_name);
        commentTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_content);

        fileImageView = (ImageView) rootView.findViewById(R.id.img_message_commented_photo);
        fileImageRound = rootView.findViewById(R.id.img_message_commented_photo_round);

        disableCoverView = rootView.findViewById(R.id.view_entity_listitem_warning);
        disableLineThroughView = rootView.findViewById(R.id.iv_entity_listitem_line_through);

        context = rootView.getContext();
        lastReadView = rootView.findViewById(R.id.vg_message_last_read);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {

        int fromEntityId = link.fromEntity;

        FormattedEntity entity = EntityManager.getInstance().getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = entity.getUserLargeProfileUrl();

        Ion.with(profileImageView)
                .placeholder(R.drawable.profile_img)
                .error(R.drawable.profile_img)
                .transform(new IonCircleTransform())
                .crossfade(true)
                .load(profileUrl);

        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
        ResLeftSideMenu.User user = entityById != EntityManager.UNKNOWN_USER_ENTITY ? entityById.getUser() : null;

        FormattedEntity feedbackEntityById = entityManager.getEntityById(link.feedback.writerId);
        ResLeftSideMenu.User feedbackUser = feedbackEntityById != EntityManager.UNKNOWN_USER_ENTITY ? feedbackEntityById.getUser() : null;

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

        nameTextView.setText(fromEntity.name);

        if (link.feedback instanceof ResMessages.FileMessage) {

            ResMessages.FileMessage feedbackFileMessage = link.feedback;

            fileImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            fileImageRound.setVisibility(View.GONE);
            if (TextUtils.equals(link.feedback.status, "archived")) {
                fileOwnerTextView.setVisibility(View.GONE);
                fileOwnerPostfixTextView.setVisibility(View.GONE);

                fileNameTextView.setText(R.string.jandi_deleted_file);
                fileNameTextView.setTextColor(fileNameTextView.getResources().getColor(R.color
                        .jandi_text_light));
                fileImageView.setBackgroundDrawable(null);
                fileImageView.setImageResource(R.drawable.jandi_fl_icon_deleted);
                fileImageView.setOnClickListener(null);
            } else {
                fileOwnerTextView.setText(feedbackUser.name);
                ResMessages.FileContent content = feedbackFileMessage.content;
                fileNameTextView.setText(content.title);
                fileNameTextView.setTextColor(fileNameTextView.getResources().getColor(R.color
                        .jandi_messages_file_name));

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
                                fileImageRound.setVisibility(View.VISIBLE);
                                Ion.with(fileImageView)
                                        .placeholder(R.drawable.file_icon_img)
                                        .error(R.drawable.file_icon_img)
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

        if (link.message instanceof ResMessages.CommentMessage) {
            ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) link.message;

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(!TextUtils.isEmpty(commentMessage.content.body) ? commentMessage.content.body : "");

            boolean hasLink = LinkifyUtil.addLinks(context, builder);

            int startIndex = builder.length();
            builder.append(DateTransformator.getTimeStringForSimple(link.message.createTime));
            int endIndex = builder.length();

            DateViewSpannable spannable =
                    new DateViewSpannable(commentTextView.getContext(),
                            DateTransformator.getTimeStringForSimple(link.message.createTime));
            builder.setSpan(spannable, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
                    link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());

            if (unreadCount > 0) {
                NameSpannable unreadCountSpannable =
                        new NameSpannable(
                                context.getResources().getDimensionPixelSize(R.dimen.jandi_text_size_small)
                                , context.getResources().getColor(R.color.jandi_accent_color));
                builder.append("  ");
                int beforeLength = builder.length();
                builder.append(" ");
                builder.append(String.valueOf(unreadCount))
                        .setSpan(unreadCountSpannable, beforeLength, builder.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }


            GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
                    commentTextView, builder, commentMessage.mentions, entityManager.getMe().getId())
                    .setPxSize(R.dimen.jandi_mention_comment_item_font_size);
            builder = generateMentionMessageUtil.generate(true);


            if (hasLink) {
                commentTextView.setText(
                        Spannable.Factory.getInstance().newSpannable(builder));

                LinkifyUtil.setOnLinkClick(commentTextView);
            } else {
                commentTextView.setText(builder);
            }

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
        return R.layout.item_message_cmt_with_file_v2;
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
