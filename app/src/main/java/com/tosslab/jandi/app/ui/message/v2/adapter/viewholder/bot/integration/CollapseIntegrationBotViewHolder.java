package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.integration;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.markdown.MarkdownLookup;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.integration.util.IntegrationBotUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

public class CollapseIntegrationBotViewHolder implements BodyViewHolder {

    private View contentView;
    private TextView tvMessage;
    private View vConnectLine;
    private LinearLayout vgConnectInfo;
    //    private ImageView ivConnectImage;
    private View vLastRead;

    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_message_item);
        tvMessage = (TextView) rootView.findViewById(R.id.tv_message_content);
        vConnectLine = rootView.findViewById(R.id.v_message_sub_menu_connect_color);
        vgConnectInfo = ((LinearLayout) rootView.findViewById(R.id.vg_message_sub_menu));
//        ivConnectImage = ((ImageView) rootView.findViewById(R.id.iv_message_sub_menu_connect_image));
        vLastRead = rootView.findViewById(R.id.vg_message_last_read);


    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {

//        int fromEntityId = link.fromEntity;
//
//        EntityManager entityManager = EntityManager.getInstance();
//        FormattedEntity entity = entityManager.getEntityById(fromEntityId);
//        if (!(entity instanceof BotEntity)) {
//            return;
//        }

        ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
        String message = textMessage.content.body;

        SpannableStringBuilder builder = MarkdownLookup.text(message).lookUp(tvMessage.getContext());
        Context context = tvMessage.getContext();

        boolean hasLink = LinkifyUtil.addLinks(context, builder);
//        if (hasLink) {
        Spannable linkSpannable = Spannable.Factory.getInstance().newSpannable(builder);
        builder.setSpan(linkSpannable, 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        LinkifyUtil.setOnLinkClick(tvMessage);
//        }

        builder.append(" ");

        int startIndex = builder.length();
        builder.append(DateTransformator.getTimeStringForSimple(link.message.createTime));
        int endIndex = builder.length();

        DateViewSpannable spannable =
                new DateViewSpannable(tvMessage.getContext(),
                        DateTransformator.getTimeStringForSimple(link.message.createTime));
        builder.setSpan(spannable, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        int unreadCount = 99;
        // UnreadCountUtil.getUnreadCount(teamId, roomId,
        // link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());

        if (unreadCount > 0) {
            NameSpannable unreadCountSpannable =
                    new NameSpannable(
                            context.getResources().getDimensionPixelSize(R.dimen.jandi_text_size_small)
                            , context.getResources().getColor(R.color.jandi_accent_color));
            int beforeLength = builder.length();
            builder.append(" ");
            builder.append(String.valueOf(unreadCount))
                    .setSpan(unreadCountSpannable, beforeLength, builder.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

//        GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
//                tvMessage, builder, textMessage.mentions,
//                EntityManager.getInstance().getMe().getId());
//        builder = generateMentionMessageUtil.generate(true);

        tvMessage.setText(builder);

        IntegrationBotUtil.setIntegrationSubUI(textMessage.content, vConnectLine, vgConnectInfo);

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
        return R.layout.item_message_inregration_bot_puremsg_v2;
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
