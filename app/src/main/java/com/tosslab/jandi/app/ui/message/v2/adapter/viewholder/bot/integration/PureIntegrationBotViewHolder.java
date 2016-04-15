//package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.integration;
//
//import android.content.Context;
//import android.text.SpannableStringBuilder;
//import android.text.Spanned;
//import android.view.View;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.tosslab.jandi.app.R;
//import com.tosslab.jandi.app.lists.BotEntity;
//import com.tosslab.jandi.app.lists.FormattedEntity;
//import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
//import com.tosslab.jandi.app.network.models.ResMessages;
//import com.tosslab.jandi.app.spannable.SpannableLookUp;
//import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
//import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.UnreadCountUtil;
//import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.integration.util.IntegrationBotUtil;
//import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.linkpreview.LinkPreviewViewModel;
//import com.tosslab.jandi.app.utils.DateTransformator;
//import com.tosslab.jandi.app.utils.LinkifyUtil;
//import com.tosslab.jandi.app.views.spannable.DateViewSpannable;
//import com.tosslab.jandi.app.views.spannable.NameSpannable;
//
//public class PureIntegrationBotViewHolder implements BodyViewHolder {
//
//    private View contentView;
//    private TextView tvMessage;
//    private View vConnectLine;
//    private LinearLayout vgConnectInfo;
//    private View vLastRead;
//    private TextView tvMessageTime;
//    private TextView tvMessageBadge;
//    private View vBottomMargin;
//
//    private LinkPreviewViewModel linkPreviewViewModel;
//
//    private boolean hasBottomMargin = false;
//    private boolean hasOnlyBadge = false;
//
//
//    @Override
//    public void initView(View rootView) {
//        contentView = rootView.findViewById(R.id.vg_dummy_message_item);
//        tvMessage = (TextView) rootView.findViewById(R.id.tv_message_content);
//        vConnectLine = rootView.findViewById(R.id.v_message_sub_menu_connect_color);
//        vgConnectInfo = ((LinearLayout) rootView.findViewById(R.id.vg_message_sub_menu));
//        tvMessageTime = (TextView) rootView.findViewById(R.id.tv_message_time);
//        tvMessageBadge = (TextView) rootView.findViewById(R.id.tv_message_badge);
//        vLastRead = rootView.findViewById(R.id.vg_message_last_read);
//
//        linkPreviewViewModel = new LinkPreviewViewModel(rootView.getContext());
//        linkPreviewViewModel.initView(rootView);
//        vBottomMargin = rootView.findViewById(R.id.v_margin);
//        if (hasBottomMargin) {
//            vBottomMargin.setVisibility(View.VISIBLE);
//        } else {
//            vBottomMargin.setVisibility(View.GONE);
//        }
//    }
//
//    @Override
//    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
//
//        long fromEntityId = link.fromEntity;
//
//        EntityManager entityManager = EntityManager.getInstance();
//        FormattedEntity entity = entityManager.getEntityById(fromEntityId);
//        if (!(entity instanceof BotEntity)) {
//            return;
//        }
//
//        ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
//        String message = textMessage.content.body;
//
//        Context context = tvMessage.getContext();
//
//        SpannableStringBuilder builder = SpannableLookUp.text(message)
//                .hyperLink(false)
//                .markdown(false)
//                .webLink(false)
//                .emailLink(false)
//                .telLink(false)
//                .lookUp(context);
//
//        LinkifyUtil.setOnLinkClick(tvMessage);
//
//        builder.append(" ");
//
//        int startIndex = builder.length();
//        builder.append(DateTransformator.getTimeStringForSimple(link.message.createTime));
//        int endIndex = builder.length();
//
//        DateViewSpannable spannable =
//                new DateViewSpannable(tvMessage.getContext(),
//                        DateTransformator.getTimeStringForSimple(link.message.createTime));
//        builder.setSpan(spannable, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
//                link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());
//
//        if (unreadCount > 0) {
//            NameSpannable unreadCountSpannable =
//                    new NameSpannable(
//                            context.getResources().getDimensionPixelSize(R.dimen.jandi_text_size_small)
//                            , context.getResources().getColor(R.color.jandi_accent_color));
//            int beforeLength = builder.length();
//            builder.append(" ");
//            builder.append(String.valueOf(unreadCount))
//                    .setSpan(unreadCountSpannable, beforeLength, builder.length(),
//                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }
//
//        tvMessage.setText(builder);
//
//        IntegrationBotUtil.setIntegrationSubUI(textMessage.content, vConnectLine, vgConnectInfo);
//
//        linkPreviewViewModel.bindData(link);
//
//    }
//
//    @Override
//    public void setLastReadViewVisible(long currentLinkId, long lastReadLinkId) {
//        if (currentLinkId == lastReadLinkId) {
//            vLastRead.setVisibility(View.VISIBLE);
//        } else {
//            vLastRead.setVisibility(View.GONE);
//        }
//    }
//
//    @Override
//    public int getLayoutId() {
//        return R.layout.item_message_inregration_bot_puremsg_v2;
//    }
//
//    @Override
//    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
//        if (contentView != null && itemClickListener != null) {
//            contentView.setOnClickListener(itemClickListener);
//        }
//    }
//
//    @Override
//    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
//        if (contentView != null && itemLongClickListener != null) {
//            contentView.setOnLongClickListener(itemLongClickListener);
//        }
//    }
//
//    public void setHasBottomMargin(boolean hasBottomMargin) {
//        this.hasBottomMargin = hasBottomMargin;
//    }
//
//    public void setHasOnlyBadge(boolean hasOnlyBadge) {
//        this.hasOnlyBadge = hasOnlyBadge;
//    }
//
//
//    public static class Builder {
//        private boolean hasBottomMargin = false;
//        private boolean hasOnlyBadge = false;
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
//        public PureIntegrationBotViewHolder build() {
//            PureIntegrationBotViewHolder collapseIntegrationBotViewHolder = new PureIntegrationBotViewHolder();
//            collapseIntegrationBotViewHolder.setHasOnlyBadge(hasOnlyBadge);
//            collapseIntegrationBotViewHolder.setHasBottomMargin(hasBottomMargin);
//            return collapseIntegrationBotViewHolder;
//        }
//    }
//}
