//package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;
//
//import android.text.SpannableStringBuilder;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.facebook.drawee.view.SimpleDraweeView;
//import com.tosslab.jandi.app.R;
//import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
//import com.tosslab.jandi.app.local.orm.domain.SendMessage;
//import com.tosslab.jandi.app.network.models.ResMessages;
//import com.tosslab.jandi.app.spannable.SpannableLookUp;
//import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
//import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
//import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
//
///**
// * Created by Steve SeongUg Jung on 15. 2. 4..
// */
//public class DummyPureViewHolder implements BodyViewHolder {
//
//    private TextView tvMessage;
//    private View contentView;
//    private SimpleDraweeView ivSticker;
//    private ImageView ivMessageStatus;
//    private ImageView ivStickerStatus;
//
//    private View vBottomMargin;
//
//    private boolean hasBottomMargin = false;
//
//    @Override
//    public void initView(View rootView) {
//        contentView = rootView.findViewById(R.id.vg_dummy_pure_message_item);
//        tvMessage = (TextView) rootView.findViewById(R.id.tv_dummy_pure_message_content);
//        ivMessageStatus = (ImageView) rootView.findViewById(R.id.iv_dummy_pure_message_send_status);
//        ivSticker = (SimpleDraweeView) rootView.findViewById(R.id.iv_dummy_pure_message_sticker);
//        ivStickerStatus = (ImageView) rootView.findViewById(R.id.iv_dummy_pure_sticker_send_status);
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
//        DummyMessageLink dummyMessageLink = (DummyMessageLink) link;
//
//        SpannableStringBuilder builder = new SpannableStringBuilder();
//
//        if (link.message instanceof ResMessages.TextMessage) {
//            ivSticker.setVisibility(View.GONE);
//            ivMessageStatus.setVisibility(View.VISIBLE);
//            ivStickerStatus.setVisibility(View.GONE);
//            tvMessage.setVisibility(View.VISIBLE);
//
//            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
//            builder.append(textMessage.content.body);
//            setTextSendingStatus(dummyMessageLink);
//
//            long myId = EntityManager.getInstance().getMe().getId();
//            MentionAnalysisInfo mentionAnalysisInfo =
//                    MentionAnalysisInfo.newBuilder(myId, ((DummyMessageLink) link).getMentions())
//                            .textSize(tvMessage.getTextSize())
//                            .build();
//
//            SpannableLookUp.text(builder)
//                    .markdown(false)
//                    .mention(mentionAnalysisInfo, false)
//                    .lookUp(contentView.getContext());
//
//            tvMessage.setText(builder);
//
//        } else if (link.message instanceof ResMessages.StickerMessage) {
//            ResMessages.StickerMessage stickerMessage = (ResMessages.StickerMessage) link.message;
//            ivSticker.setVisibility(View.VISIBLE);
//            ivMessageStatus.setVisibility(View.GONE);
//            ivStickerStatus.setVisibility(View.VISIBLE);
//            tvMessage.setVisibility(View.GONE);
//
//            ResMessages.StickerContent content = stickerMessage.content;
//
//            StickerManager.getInstance().loadStickerDefaultOption(ivSticker, content.groupId, content.stickerId);
//
//            setStickerSendingStatus(dummyMessageLink);
//        }
//    }
//
//    private void setStickerSendingStatus(DummyMessageLink dummyMessageLink) {
//        SendMessage.Status status = SendMessage.Status.valueOf(dummyMessageLink.getStatus());
//        switch (status) {
//            case COMPLETE:
//            case SENDING:
//                ivStickerStatus.setImageResource(R.drawable.icon_message_sending);
//                break;
//            case FAIL:
//                ivStickerStatus.setImageResource(R.drawable.icon_message_failure);
//                break;
//        }
//    }
//
//    private void setTextSendingStatus(DummyMessageLink dummyMessageLink) {
//        SendMessage.Status status = SendMessage.Status.valueOf(dummyMessageLink.getStatus());
//        switch (status) {
//            case COMPLETE:
//            case SENDING: {
//                ivMessageStatus.setImageResource(R.drawable.icon_message_sending);
//                break;
//            }
//            case FAIL:
//                ivMessageStatus.setImageResource(R.drawable.icon_message_failure);
//                break;
//        }
//    }
//
//    @Override
//    public void setLastReadViewVisible(long currentLinkId, long lastReadLinkId) {
//    }
//
//    @Override
//    public int getLayoutId() {
//        return R.layout.item_message_dummy_v2;
//
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
//    public static class Builder {
//        private boolean hasBottomMargin = false;
//
//        public Builder setHasBottomMargin(boolean hasBottomMargin) {
//            this.hasBottomMargin = hasBottomMargin;
//            return this;
//        }
//
//        public DummyPureViewHolder build() {
//            DummyPureViewHolder dummyPureViewHolder = new DummyPureViewHolder();
//            dummyPureViewHolder.setHasBottomMargin(hasBottomMargin);
//            return dummyPureViewHolder;
//        }
//    }
//
//}
