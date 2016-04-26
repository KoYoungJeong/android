//package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;
//
//import android.view.View;
//import android.widget.TextView;
//
//import com.facebook.drawee.view.SimpleDraweeView;
//import com.tosslab.jandi.app.R;
//import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
//import com.tosslab.jandi.app.network.models.ResMessages;
//import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
//import com.tosslab.jandi.app.utils.DateTransformator;
//
///**
// * Created by Steve SeongUg Jung on 15. 5. 6..
// */
//public class PureStickerViewHolder implements BodyViewHolder {
//
//    private SimpleDraweeView ivSticker;
//    private TextView tvDate;
//    private TextView tvUnread;
//    private View vLastRead;
//    private View contentView;
//    private boolean hasOnlyBadge;
//    private boolean hasBottomMargin;
//
//    @Override
//    public void initView(View rootView) {
//        contentView = rootView.findViewById(R.id.vg_dummy_message_item);
//        ivSticker = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_sticker);
//        tvDate = (TextView) rootView.findViewById(R.id.tv_message_create_date);
//        tvUnread = (TextView) rootView.findViewById(R.id.tv_entity_listitem_unread);
//        vLastRead = rootView.findViewById(R.id.vg_message_last_read);
//        View vBottomMargin = rootView.findViewById(R.id.v_margin);
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
//        ResMessages.StickerMessage stickerMessage = (ResMessages.StickerMessage) link.message;
//        ResMessages.StickerContent content = stickerMessage.content;
//        StickerManager.getInstance().loadStickerNoOption(ivSticker, content.groupId, content.stickerId);
//
//        if (!hasOnlyBadge) {
//            tvDate.setText(DateTransformator.getTimeStringForSimple(link.message.createTime));
//        } else {
//            tvDate.setVisibility(View.GONE);
//        }
//
//        int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
//                link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());
//
//        tvUnread.setText(String.valueOf(unreadCount));
//
//        if (unreadCount > 0) {
//            tvUnread.setVisibility(View.VISIBLE);
//        } else {
//            tvUnread.setVisibility(View.GONE);
//        }
//
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
//        return R.layout.item_message_pure_sticker_v2;
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
//    public void setHasOnlyBadge(boolean hasOnlyBadge) {
//        this.hasOnlyBadge = hasOnlyBadge;
//    }
//
//    public void setHasBottomMargin(boolean hasBottomMargin) {
//        this.hasBottomMargin = hasBottomMargin;
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
//        public PureStickerViewHolder build() {
//            PureStickerViewHolder pureStickerViewHolder = new PureStickerViewHolder();
//            pureStickerViewHolder.setHasOnlyBadge(hasOnlyBadge);
//            pureStickerViewHolder.setHasBottomMargin(hasBottomMargin);
//            return pureStickerViewHolder;
//        }
//    }
//}
