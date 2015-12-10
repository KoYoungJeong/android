package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.integration;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.markdown.MarkdownLookUp;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.integration.util.IntegrationBotUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

public class IntegrationBotViewHolder implements BodyViewHolder {

    private static final String TAG = "IntegrationBotViewHolder";
    private View contentView;
    private ImageView ivProfile;
    private TextView tvName;
    private TextView tvMessage;
    private View vDisableCover;
    private View vDisableLineThrough;
    private View vConnectLine;
    private LinearLayout vgConnectInfo;
    //    private ImageView ivConnectImage;
    private View vLastRead;

    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_message_item);
        ivProfile = (ImageView) rootView.findViewById(R.id.iv_message_user_profile);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
        tvMessage = (TextView) rootView.findViewById(R.id.tv_message_content);
        vDisableCover = rootView.findViewById(R.id.v_entity_listitem_warning);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);
        vConnectLine = rootView.findViewById(R.id.v_message_sub_menu_connect_color);
        vgConnectInfo = ((LinearLayout) rootView.findViewById(R.id.vg_message_sub_menu));
//        ivConnectImage = ((ImageView) rootView.findViewById(R.id.iv_message_sub_menu_connect_image));
        vLastRead = rootView.findViewById(R.id.vg_message_last_read);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {
//        int fromEntityId = link.fromEntity;

//        EntityManager entityManager = EntityManager.getInstance();
//        FormattedEntity entity = entityManager.getEntityById(fromEntityId);
//        if (!(entity instanceof BotEntity)) {
//            return;
//        }

//        BotEntity botEntity = (BotEntity) entity;
//        ResLeftSideMenu.Bot bot = botEntity.getBot();

        ivProfile.setImageResource(R.drawable.profile_img);

//        if (bot != null && TextUtils.equals(bot.status, "enabled")) {
        tvName.setTextColor(tvName.getResources().getColor(R.color.jandi_messages_name));
        vDisableCover.setVisibility(View.GONE);
        vDisableLineThrough.setVisibility(View.GONE);
//        } else {
//            tvName.setTextColor(
//                    tvName.getResources().getColor(R.color.deactivate_text_color));
//            vDisableCover.setVisibility(View.VISIBLE);
//            vDisableLineThrough.setVisibility(View.VISIBLE);
//        }

        ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;

        SpannableStringBuilder messageStringBuilder = MarkdownLookUp
                .text(textMessage.content.body)
                .lookUp(tvMessage.getContext());

        Context context = tvMessage.getContext();

        boolean hasLink = LinkifyUtil.addLinks(context, messageStringBuilder);
//        if (hasLink) {
        Spannable linkSpannable =
                Spannable.Factory.getInstance().newSpannable(messageStringBuilder);
        messageStringBuilder.setSpan(linkSpannable,
                0, messageStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        LinkifyUtil.setOnLinkClick(tvMessage);
//        }

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

        int unreadCount = 99;
//                UnreadCountUtil.getUnreadCount(teamId, roomId,
//                link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());

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

//        GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
//                tvMessage, messageStringBuilder, textMessage.mentions, entityManager.getMe().getId());
//        messageStringBuilder = generateMentionMessageUtil.generate(true);

        tvMessage.setText(messageStringBuilder);

        IntegrationBotUtil.setIntegrationSubUI(textMessage.content, vConnectLine, vgConnectInfo);

        tvName.setText("TEST!!!!");


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
