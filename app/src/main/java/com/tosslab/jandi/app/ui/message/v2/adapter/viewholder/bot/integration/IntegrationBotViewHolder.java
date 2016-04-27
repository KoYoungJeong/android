package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.integration;

import android.content.Context;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.UnreadCountUtil;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.integration.util.IntegrationBotUtil;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.linkpreview.LinkPreviewViewModel;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.transform.TransformConfig;

public class IntegrationBotViewHolder implements BodyViewHolder {

    private static final String TAG = "IntegrationBotViewHolder";
    private View contentView;
    private SimpleDraweeView ivProfile;
    private TextView tvName;
    private TextView tvMessage;
    private View vDisableCover;
    private View vDisableLineThrough;
    private View vConnectLine;
    private TextView tvMessageTime;
    private TextView tvMessageBadge;
    private LinearLayout vgConnectInfo;
    private View vLastRead;
    private View vBottomMargin;

    private boolean hasBottomMargin = false;
    private boolean hasOnlyBadge = false;

    private LinkPreviewViewModel linkPreviewViewModel;
    private boolean hasBotProfile;
    private ViewGroup vgUserName;

    private IntegrationBotViewHolder() {
    }


    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_dummy_message_item);
        ivProfile = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_user_profile);
        vgUserName = (ViewGroup) rootView.findViewById(R.id.vg_message_profile_user_name);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
        tvMessage = (TextView) rootView.findViewById(R.id.tv_message_content);
        tvMessageTime = (TextView) rootView.findViewById(R.id.tv_message_time);
        tvMessageBadge = (TextView) rootView.findViewById(R.id.tv_message_badge);
        vDisableCover = rootView.findViewById(R.id.v_entity_listitem_warning);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);
        vConnectLine = rootView.findViewById(R.id.v_message_sub_menu_connect_color);
        vgConnectInfo = ((LinearLayout) rootView.findViewById(R.id.vg_message_sub_menu));
        vLastRead = rootView.findViewById(R.id.vg_message_last_read);

        linkPreviewViewModel = new LinkPreviewViewModel(rootView.getContext());
        linkPreviewViewModel.initView(rootView);

        vBottomMargin = rootView.findViewById(R.id.v_margin);

        if (hasBottomMargin) {
            vBottomMargin.setVisibility(View.VISIBLE);
        } else {
            vBottomMargin.setVisibility(View.GONE);
        }

        if (hasBotProfile) {
            ivProfile.setVisibility(View.VISIBLE);
            vgUserName.setVisibility(View.VISIBLE);
        } else {
            ivProfile.setVisibility(View.INVISIBLE);
            vgUserName.setVisibility(View.GONE);
        }

    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        long fromEntityId = link.fromEntity;

        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entity = entityManager.getEntityById(fromEntityId);
        if (!(entity instanceof BotEntity)) {
            return;
        }

        BotEntity botEntity = (BotEntity) entity;
        ResLeftSideMenu.Bot bot = botEntity.getBot();

        RoundingParams circleRoundingParams = ImageUtil.getCircleRoundingParams(
                TransformConfig.DEFAULT_CIRCLE_LINE_COLOR, TransformConfig.DEFAULT_CIRCLE_LINE_WIDTH);

        ImageLoader.newBuilder()
                .placeHolder(R.drawable.profile_img, ScalingUtils.ScaleType.FIT_CENTER)
                .actualScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .roundingParams(circleRoundingParams)
                .load(Uri.parse(botEntity.getUserLargeProfileUrl()))
                .into(ivProfile);

        tvName.setText(botEntity.getName());

        if (bot != null && TextUtils.equals(bot.status, "enabled")) {
            tvName.setTextColor(tvName.getResources().getColor(R.color.jandi_messages_name));
            vDisableCover.setVisibility(View.GONE);
            vDisableLineThrough.setVisibility(View.GONE);
        } else {
            tvName.setTextColor(
                    tvName.getResources().getColor(R.color.deactivate_text_color));
            vDisableCover.setVisibility(View.VISIBLE);
            vDisableLineThrough.setVisibility(View.VISIBLE);
        }

        ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;

        Context context = tvMessage.getContext();

        SpannableStringBuilder messageStringBuilder = SpannableLookUp.text(textMessage.content.body)
                .hyperLink(false)
                .markdown(false)
                .webLink(false)
                .emailLink(false)
                .telLink(false)
                .lookUp(context);

        LinkifyUtil.setOnLinkClick(tvMessage);

        int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
                link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());

        if (!hasOnlyBadge) {
            tvMessageTime.setVisibility(View.VISIBLE);
            tvMessageTime.setText(DateTransformator.getTimeStringForSimple(link.message.createTime));
        } else {
            tvMessageTime.setVisibility(View.GONE);
        }

        if (unreadCount > 0) {
            tvMessageBadge.setText(String.valueOf(unreadCount));
        }

        tvMessage.setText(messageStringBuilder);

        IntegrationBotUtil.setIntegrationSubUI(textMessage.content, vConnectLine, vgConnectInfo);

        linkPreviewViewModel.bindData(link);

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
        return R.layout.item_message_integration_bot_msg_v3;
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

    public void setHasBottomMargin(boolean hasBottomMargin) {
        this.hasBottomMargin = hasBottomMargin;
    }

    public void setHasOnlyBadge(boolean hasOnlyBadge) {
        this.hasOnlyBadge = hasOnlyBadge;
    }

    public void setHasBotProfile(boolean hasBotProfile) {
        this.hasBotProfile = hasBotProfile;
    }


    public static class Builder extends BaseViewHolderBuilder {
//        private boolean hasBottomMargin = false;
//        private boolean hasOnlyBadge = false;
//        private boolean hasProfile = false;
//
//        public Builder setHasBottomMargin(boolean hasBottomMargin) {
//            this.hasBottomMargin = hasBottomMargin;
//            return this;
//        }
//
//        public Builder setHasOnlyBadge(boolean hasOnlyBadge) {
//            this.hasOnlyBadge = hasOnlyBadge;
//            return this;
//        }
//
//        public Builder setHasBotProfile(boolean hasProfile) {
//            this.hasProfile = hasProfile;
//            return this;
//        }

        public IntegrationBotViewHolder build() {
            IntegrationBotViewHolder integrationBotViewHolder = new IntegrationBotViewHolder();
            integrationBotViewHolder.setHasOnlyBadge(hasOnlyBadge);
            integrationBotViewHolder.setHasBottomMargin(hasBottomMargin);
            integrationBotViewHolder.setHasBotProfile(hasProfile);
            return integrationBotViewHolder;
        }
    }

}
