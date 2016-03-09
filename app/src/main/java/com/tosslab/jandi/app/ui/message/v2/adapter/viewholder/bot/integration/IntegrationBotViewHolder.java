package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.integration;

import android.content.Context;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
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
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.linkpreview.LinkPreviewViewModel;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.transform.TransformConfig;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

public class IntegrationBotViewHolder implements BodyViewHolder {

    private static final String TAG = "IntegrationBotViewHolder";
    private View contentView;
    private SimpleDraweeView ivProfile;
    private TextView tvName;
    private TextView tvMessage;
    private View vDisableCover;
    private View vDisableLineThrough;
    private View vConnectLine;
    private LinearLayout vgConnectInfo;
    private View vLastRead;

    private LinkPreviewViewModel linkPreviewViewModel;

    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_message_item);
        ivProfile = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_user_profile);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
        tvMessage = (TextView) rootView.findViewById(R.id.tv_message_content);
        vDisableCover = rootView.findViewById(R.id.v_entity_listitem_warning);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);
        vConnectLine = rootView.findViewById(R.id.v_message_sub_menu_connect_color);
        vgConnectInfo = ((LinearLayout) rootView.findViewById(R.id.vg_message_sub_menu));
        vLastRead = rootView.findViewById(R.id.vg_message_last_read);

        linkPreviewViewModel = new LinkPreviewViewModel(rootView.getContext());
        linkPreviewViewModel.initView(rootView);
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

        messageStringBuilder.append(" ");

        int startIndex = messageStringBuilder.length();
        messageStringBuilder.append(
                DateTransformator.getTimeStringForSimple(link.message.createTime));
        int endIndex = messageStringBuilder.length();

        DateViewSpannable spannable =
                new DateViewSpannable(tvMessage.getContext(),
                        DateTransformator.getTimeStringForSimple(link.message.createTime));
        messageStringBuilder.setSpan(spannable,
                startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
                link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());

        if (unreadCount > 0) {
            NameSpannable unreadCountSpannable =
                    new NameSpannable(
                            context.getResources().getDimensionPixelSize(R.dimen.jandi_text_size_small)
                            , context.getResources().getColor(R.color.jandi_accent_color));
            int beforeLength = messageStringBuilder.length();
            messageStringBuilder.append(" ");
            messageStringBuilder.append(String.valueOf(unreadCount))
                    .setSpan(unreadCountSpannable, beforeLength, messageStringBuilder.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        return R.layout.item_message_integration_bot_msg_v2;
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
