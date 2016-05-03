package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class StickerMessageViewHolder extends BaseMessageViewHolder {

    protected Context context;

    private SimpleDraweeView ivProfile;

    private TextView tvName;
    private View vDisableLineThrough;
    private SimpleDraweeView ivSticker;

    private StickerMessageViewHolder() {
    }

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        context = rootView.getContext();
        ivProfile = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_user_profile);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);
        ivSticker = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_sticker);
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        if (hasProfile) {
            ivProfile.setVisibility(View.VISIBLE);
            tvName.setVisibility(View.VISIBLE);
            vDisableLineThrough.setVisibility(View.VISIBLE);
            setProfileInfos(link);
        } else {
            ivProfile.setVisibility(View.GONE);
            tvName.setVisibility(View.GONE);
            vDisableLineThrough.setVisibility(View.GONE);
        }
        setBadge(link, teamId, roomId);
        setTime(link);
        setSticker(link);
    }

    private void setTime(ResMessages.Link link) {
        if (!hasOnlyBadge) {
            tvMessageTime.setText(DateTransformator.getTimeStringForSimple(link.message.createTime));
        } else {
            tvMessageTime.setVisibility(View.GONE);
        }
    }

    private void setSticker(ResMessages.Link link) {
        ResMessages.StickerMessage stickerMessage = (ResMessages.StickerMessage) link.message;
        ResMessages.StickerContent content = stickerMessage.content;

        StickerManager.getInstance().loadStickerNoOption(ivSticker, content.groupId, content.stickerId);
    }

    private void setBadge(ResMessages.Link link, long teamId, long roomId) {

        int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
                link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());
        tvMessageBadge.setText(String.valueOf(unreadCount));

        if (unreadCount > 0) {
            tvMessageBadge.setVisibility(View.VISIBLE);
        } else {
            tvMessageBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_sticker_v3;
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        ivSticker.setOnClickListener(itemClickListener);
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        ivSticker.setOnLongClickListener(itemLongClickListener);
    }

    public void setProfileInfos(ResMessages.Link link) {
        long fromEntityId = link.fromEntity;

        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entity = entityManager.getEntityById(fromEntityId);

        String profileUrl = entity.getUserLargeProfileUrl();

        ImageUtil.loadProfileImage(ivProfile, profileUrl, R.drawable.profile_img);

        if (entity.getUser() != null && entity.isEnabled()) {
            tvName.setTextColor(context.getResources().getColor(R.color.jandi_messages_name));
            vDisableLineThrough.setVisibility(View.GONE);
        } else {
            tvName.setTextColor(
                    tvName.getResources().getColor(R.color.deactivate_text_color));
            vDisableLineThrough.setVisibility(View.VISIBLE);
        }
        tvName.setText(entity.getName());

        ivProfile.setOnClickListener(v -> EventBus.getDefault().post(
                new ShowProfileEvent(fromEntityId, ShowProfileEvent.From.Image)));
        tvName.setOnClickListener(v -> EventBus.getDefault().post(
                new ShowProfileEvent(fromEntityId, ShowProfileEvent.From.Name)));
    }

    public static class Builder extends BaseViewHolderBuilder {

        public StickerMessageViewHolder build() {
            StickerMessageViewHolder messageViewHolder = new StickerMessageViewHolder();
            messageViewHolder.setHasOnlyBadge(hasOnlyBadge);
            messageViewHolder.setHasBottomMargin(hasBottomMargin);
            messageViewHolder.setHasProfile(hasProfile);
            return messageViewHolder;
        }
    }

}