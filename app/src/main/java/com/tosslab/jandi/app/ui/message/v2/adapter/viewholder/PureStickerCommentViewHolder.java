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
// * Created by tee on 16. 4. 7..
// */
//public class PureStickerCommentViewHolder extends BaseCommentViewHolder {
//
//    private TextView tvStickerCommentUnread;
//    private TextView tvPureCommentStickerCreateDate;
//    private SimpleDraweeView ivPureCommentSticker;
//
//    @Override
//    public void initView(View rootView) {
//        super.initView(rootView);
//        ivPureCommentSticker = (SimpleDraweeView) rootView.findViewById(R.id.iv_pure_comment_sticker);
//        tvPureCommentStickerCreateDate =
//                (TextView) rootView.findViewById(R.id.tv_pure_comment_sticker_create_date);
//        tvStickerCommentUnread =
//                (TextView) rootView.findViewById(R.id.tv_sticker_comment_unread);
//    }
//
//    @Override
//    protected void initObjects() {
//        vgFileItem.setVisibility(View.GONE);
//        vgProfileNestedComment.setVisibility(View.GONE);
//        vgProfileNestedCommentSticker.setVisibility(View.GONE);
//        tvPureComment.setVisibility(View.GONE);
//        vgPureCommentSticker.setVisibility(View.VISIBLE);
//        ivCommentBubbleTail.setVisibility(View.GONE);
//    }
//
//    @Override
//    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
//        super.bindData(link, teamId, roomId, entityId);
//        getStickerComment(link, teamId, roomId);
//    }
//
//    private void getStickerComment(ResMessages.Link link, long teamId, long roomId) {
//        ResMessages.CommentStickerMessage message = (ResMessages.CommentStickerMessage) link.message;
//
//        StickerManager.getInstance().loadStickerNoOption(ivPureCommentSticker, message.content.groupId, message.content.stickerId);
//
//        tvPureCommentStickerCreateDate.setText(DateTransformator.getTimeStringForSimple(message.createTime));
//        int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
//                link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());
//
//        if (unreadCount > 0) {
//            tvStickerCommentUnread.setText(String.valueOf(unreadCount));
//            tvStickerCommentUnread.setVisibility(View.VISIBLE);
//        } else {
//            tvStickerCommentUnread.setVisibility(View.GONE);
//        }
//    }
//
//    public static class Builder {
//        private boolean hasBottomMargin = false;
//        private boolean hasSemiDivider = false;
//
//        public Builder setHasBottomMargin(boolean hasBottomMargin) {
//            this.hasBottomMargin = hasBottomMargin;
//            return this;
//        }
//
//        public Builder setHasSemiDivider(boolean hasSemiDivider) {
//            this.hasSemiDivider = hasSemiDivider;
//            return this;
//        }
//
//        public PureStickerCommentViewHolder build() {
//            PureStickerCommentViewHolder viewHolder = new PureStickerCommentViewHolder();
//            viewHolder.setHasBottomMargin(hasBottomMargin);
//            viewHolder.setHasSemiDivider(hasSemiDivider);
//            return viewHolder;
//        }
//    }
//
//}
