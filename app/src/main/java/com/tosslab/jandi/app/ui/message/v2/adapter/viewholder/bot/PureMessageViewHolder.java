//package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot;
//
//import android.text.SpannableStringBuilder;
//import android.text.TextUtils;
//import android.view.View;
//import android.widget.TextView;
//
//import com.tosslab.jandi.app.R;
//import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
//import com.tosslab.jandi.app.network.models.ResMessages;
//import com.tosslab.jandi.app.spannable.SpannableLookUp;
//import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
//import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
//import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.UnreadCountUtil;
//import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.linkpreview.LinkPreviewViewModel;
//import com.tosslab.jandi.app.utils.DateTransformator;
//import com.tosslab.jandi.app.utils.LinkifyUtil;
//
//public class PureMessageViewHolder implements BodyViewHolder {
//
//    private TextView tvMessage;
//    private LinkPreviewViewModel linkPreviewViewModel;
//    private View vLastRead;
//    private View contentView;
//    private TextView tvMessageTime;
//    private TextView tvMessageBadge;
//    private View vBottomMargin;
//
//    private boolean hasBottomMargin = false;
//    private boolean hasOnlyBadge = false;
//
//    @Override
//    public void initView(View rootView) {
//        contentView = rootView.findViewById(R.id.vg_dummy_message_item);
//        tvMessage = (TextView) rootView.findViewById(R.id.tv_message_content);
//        tvMessageTime = (TextView) rootView.findViewById(R.id.tv_message_time);
//        tvMessageBadge = (TextView) rootView.findViewById(R.id.tv_message_badge);
//        linkPreviewViewModel = new LinkPreviewViewModel(rootView.getContext());
//        linkPreviewViewModel.initView(rootView);
//        vLastRead = rootView.findViewById(R.id.vg_message_last_read);
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
//        ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
//        String message = textMessage.content.body;
//
//        SpannableStringBuilder messageStringBuilder = new SpannableStringBuilder();
//        messageStringBuilder.append(!TextUtils.isEmpty(message) ? message + '\u200e' : "");
//
//        EntityManager entityManager = EntityManager.getInstance();
//        long myId = entityManager.getMe().getId();
//        MentionAnalysisInfo mentionInfo = MentionAnalysisInfo.newBuilder(myId, textMessage.mentions)
//                .textSize(tvMessage.getTextSize())
//                .clickable(true)
//                .build();
//
//        SpannableLookUp.text(messageStringBuilder)
//                .hyperLink(false)
//                .markdown(false)
//                .webLink(false)
//                .telLink(false)
//                .emailLink(false)
//                .mention(mentionInfo, false)
//                .lookUp(tvMessage.getContext());
//        LinkifyUtil.setOnLinkClick(tvMessage);
//
//        int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
//                link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());
//        if (unreadCount > 0) {
//            tvMessageBadge.setText(String.valueOf(unreadCount));
//        }
//
//        if (!hasOnlyBadge) {
//            tvMessageTime.setVisibility(View.VISIBLE);
//            tvMessageTime.setText(DateTransformator.getTimeStringForSimple(link.message.createTime));
//        } else {
//            tvMessageTime.setVisibility(View.GONE);
//        }
//
//        tvMessage.setText(messageStringBuilder);
//
//        linkPreviewViewModel.bindData(link);
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
//        return R.layout.item_message_puremsg_v2;
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
//        public PureMessageViewHolder build() {
//            PureMessageViewHolder messageViewHolder
//                    = new PureMessageViewHolder();
//            messageViewHolder.setHasOnlyBadge(hasOnlyBadge);
//            messageViewHolder.setHasBottomMargin(hasBottomMargin);
//            return messageViewHolder;
//        }
//    }
//}