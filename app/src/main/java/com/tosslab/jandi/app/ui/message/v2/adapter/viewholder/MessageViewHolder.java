package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.linkpreview.LinkPreviewViewModel;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class MessageViewHolder extends BaseMessageViewHolder {

    protected Context context;

    private SimpleDraweeView ivProfile;

    private TextView tvName;
    private View vDisableCover;
    private View vDisableLineThrough;

    private TextView tvMessage;
    private LinkPreviewViewModel linkPreviewViewModel;

    private SimpleDraweeView ivSticker;

    private boolean isPure = false;

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        context = rootView.getContext();

        ivProfile = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_user_profile);

        tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
        vDisableCover = rootView.findViewById(R.id.v_entity_listitem_warning);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);

        tvMessage = (TextView) rootView.findViewById(R.id.tv_message_content);

        ivSticker = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_sticker);

        linkPreviewViewModel = new LinkPreviewViewModel(context);
        linkPreviewViewModel.initView(rootView);
    }

    @Override
    protected void initObjects() {
        vgFileMessageContent.setVisibility(View.GONE);
        vgImageMessageContent.setVisibility(View.GONE);
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        if (!isPure) {
            setProfileInfos(link);
        }
        setMessage(link, teamId, roomId);
    }

    private void setMessage(ResMessages.Link link, long teamId, long roomId) {

        if (link.message instanceof ResMessages.TextMessage) {
            vgMessageContent.setVisibility(View.VISIBLE);
            vgStickerMessageContent.setVisibility(View.GONE);

            EntityManager entityManager = EntityManager.getInstance();
            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;

            SpannableStringBuilder messageStringBuilder = new SpannableStringBuilder();
            messageStringBuilder.append(!TextUtils.isEmpty(textMessage.content.body) ? textMessage.content.body + '\u200e' : "");

            long myId = entityManager.getMe().getId();

            MentionAnalysisInfo mentionInfo = MentionAnalysisInfo.newBuilder(myId, textMessage.mentions)
                    .textSize(tvMessage.getTextSize())
                    .clickable(true)
                    .build();

            SpannableLookUp.text(messageStringBuilder)
                    .hyperLink(false)
                    .markdown(false)
                    .webLink(false)
                    .telLink(false)
                    .emailLink(false)
                    .mention(mentionInfo, false)
                    .lookUp(tvMessage.getContext());

            LinkifyUtil.setOnLinkClick(tvMessage);

            int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
                    link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());

            if (!hasOnlyBadge) {
                tvMessageTime.setText(DateTransformator.getTimeStringForSimple(link.message.createTime));
            } else {
                tvMessageTime.setVisibility(View.GONE);
            }

            if (unreadCount > 0) {
                tvMessageBadge.setText(String.valueOf(unreadCount));
            }

            tvMessage.setText(messageStringBuilder);
            linkPreviewViewModel.bindData(link);
        } else if (link.message instanceof ResMessages.StickerMessage) {
            vgMessageContent.setVisibility(View.GONE);
            vgStickerMessageContent.setVisibility(View.VISIBLE);
            if (!hasOnlyBadge) {
                tvMessageTime.setText(DateTransformator.getTimeStringForSimple(link.message.createTime));
            } else {
                tvMessageTime.setVisibility(View.GONE);
            }
            int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
                    link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());

            tvMessageBadge.setText(String.valueOf(unreadCount));

            if (unreadCount > 0) {
                tvMessageBadge.setVisibility(View.VISIBLE);
            } else {
                tvMessageBadge.setVisibility(View.GONE);
            }

            ResMessages.StickerMessage stickerMessage = (ResMessages.StickerMessage) link.message;
            ResMessages.StickerContent content = stickerMessage.content;

            StickerManager.getInstance().loadStickerNoOption(ivSticker, content.groupId, content.stickerId);
        }

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

    public static class Builder {
        private boolean hasBottomMargin = false;
        private boolean hasOnlyBadge = false;
        private boolean hasProfile = false;

        public Builder setHasBottomMargin(boolean hasBottomMargin) {
            this.hasBottomMargin = hasBottomMargin;
            return this;
        }

        public Builder setHasOnlyBadge(boolean hasOnlyBadge) {
            this.hasOnlyBadge = hasOnlyBadge;
            return this;
        }

        public Builder setHasUserProfile(boolean hasProfile) {
            this.hasProfile = hasProfile;
            return this;
        }

        public MessageViewHolder build() {
            MessageViewHolder messageViewHolder = new MessageViewHolder();
            messageViewHolder.setHasOnlyBadge(hasOnlyBadge);
            messageViewHolder.setHasBottomMargin(hasBottomMargin);
            messageViewHolder.setHasProfile(hasProfile);
            return messageViewHolder;
        }
    }

}
