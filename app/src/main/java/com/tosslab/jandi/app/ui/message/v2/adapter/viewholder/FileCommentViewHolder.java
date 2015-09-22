package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
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
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.GenerateMentionMessageUtil;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;
import com.tosslab.jandi.app.utils.transform.ion.IonCircleTransform;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class FileCommentViewHolder implements BodyViewHolder {

    private ImageView ivProfile;
    private TextView tvName;
    private TextView tvFileOwner;
    private TextView tvFileName;
    private TextView tvComment;
    private ImageView ivFileImage;
    private View vDisableCover;
    private View vDisableLineThrough;
    private Context context;
    private View vLastRead;
    private View vFileImageRound;
    private View contentView;

    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_message_item);
        ivProfile = (ImageView) rootView.findViewById(R.id.iv_message_user_profile);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);

        tvFileOwner = (TextView) rootView.findViewById(R.id.tv_message_commented_owner);
        tvFileName = (TextView) rootView.findViewById(R.id.tv_message_commented_file_name);
        tvComment = (TextView) rootView.findViewById(R.id.tv_message_commented_content);

        ivFileImage = (ImageView) rootView.findViewById(R.id.iv_message_commented_photo);
        vFileImageRound = rootView.findViewById(R.id.iv_message_commented_photo_round);

        vDisableCover = rootView.findViewById(R.id.v_entity_listitem_warning);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);

        context = rootView.getContext();
        vLastRead = rootView.findViewById(R.id.vg_message_last_read);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {

        int fromEntityId = link.fromEntity;

        FormattedEntity entity = EntityManager.getInstance().getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = entity.getUserLargeProfileUrl();

        Ion.with(ivProfile)
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
            tvName.setTextColor(context.getResources().getColor(R.color.jandi_messages_name));
            vDisableCover.setVisibility(View.GONE);
            vDisableLineThrough.setVisibility(View.GONE);
        } else {
            tvName.setTextColor(
                    context.getResources().getColor(R.color.deactivate_text_color));
            vDisableCover.setVisibility(View.VISIBLE);
            vDisableLineThrough.setVisibility(View.VISIBLE);
        }

        tvName.setText(fromEntity.name);

        if (link.feedback instanceof ResMessages.FileMessage) {

            ResMessages.FileMessage feedbackFileMessage = link.feedback;

            ivFileImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            vFileImageRound.setVisibility(View.GONE);
            if (TextUtils.equals(link.feedback.status, "archived")) {
                tvFileOwner.setVisibility(View.GONE);

                tvFileName.setText(R.string.jandi_deleted_file);
                tvFileName.setTextColor(tvFileName.getResources().getColor(R.color
                        .jandi_text_light));
                ivFileImage.setBackgroundDrawable(null);
                ivFileImage.setImageResource(R.drawable.jandi_fl_icon_deleted);
                ivFileImage.setOnClickListener(null);
            } else {
                tvFileOwner.setText(Html.fromHtml(tvFileOwner.getResources().getString(R.string.jandi_commented_on, feedbackUser.name)));
                ResMessages.FileContent content = feedbackFileMessage.content;
                tvFileName.setText(content.title);
                tvFileName.setTextColor(tvFileName.getResources().getColor(R.color
                        .jandi_messages_file_name));

                tvFileOwner.setVisibility(View.VISIBLE);

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

        if (link.message instanceof ResMessages.CommentMessage) {
            ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) link.message;

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(!TextUtils.isEmpty(commentMessage.content.body) ? commentMessage.content.body : "");
            builder.append(" ");

            boolean hasLink = LinkifyUtil.addLinks(context, builder);

            int startIndex = builder.length();
            builder.append(DateTransformator.getTimeStringForSimple(link.message.createTime));
            int endIndex = builder.length();

            DateViewSpannable spannable =
                    new DateViewSpannable(tvComment.getContext(),
                            DateTransformator.getTimeStringForSimple(link.message.createTime));
            builder.setSpan(spannable, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
                    link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());

            if (unreadCount > 0) {
                NameSpannable unreadCountSpannable =
                        new NameSpannable(
                                context.getResources().getDimensionPixelSize(R.dimen.jandi_text_size_small)
                                , context.getResources().getColor(R.color.jandi_accent_color));
                builder.append(" ");
                int beforeLength = builder.length();
                builder.append(" ");
                builder.append(String.valueOf(unreadCount))
                        .setSpan(unreadCountSpannable, beforeLength, builder.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }


            GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
                    tvComment, builder, commentMessage.mentions, entityManager.getMe().getId())
                    .setPxSize(R.dimen.jandi_mention_comment_item_font_size);
            builder = generateMentionMessageUtil.generate(true);


            if (hasLink) {
                tvComment.setText(
                        Spannable.Factory.getInstance().newSpannable(builder));

                LinkifyUtil.setOnLinkClick(tvComment);
            } else {
                tvComment.setText(builder);
            }

        }

        ivProfile.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Image)));
        tvName.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Name)));
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
