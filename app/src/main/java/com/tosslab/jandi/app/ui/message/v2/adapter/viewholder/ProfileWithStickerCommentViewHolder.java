//package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;
//
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.facebook.drawee.view.SimpleDraweeView;
//import com.tosslab.jandi.app.JandiApplication;
//import com.tosslab.jandi.app.R;
//import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
//import com.tosslab.jandi.app.lists.FormattedEntity;
//import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
//import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
//import com.tosslab.jandi.app.network.models.ResMessages;
//import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
//import com.tosslab.jandi.app.utils.DateTransformator;
//import com.tosslab.jandi.app.utils.image.ImageUtil;
//
//import de.greenrobot.event.EventBus;
//
///**
// * Created by tee on 16. 4. 7..
// */
//public class ProfileWithStickerCommentViewHolder extends BaseCommentViewHolder {
//
//    private SimpleDraweeView ivProfileNestedUserProfileForSticker;
//    private TextView tvProfileNestedUserNameForSticker;
//    private ImageView ivProfileNestedLineThroughForSticker;
//    private SimpleDraweeView ivProfileNestedCommentSticker;
//    private TextView tvProfileNestedCommentStickerCreateDate;
//    private TextView tvProfileNestedCommentStickerUnread;
//
//    @Override
//    public void initView(View rootView) {
//        super.initView(rootView);
//
//        // 프로필이 있는 커멘트 스티커
//        ivProfileNestedUserProfileForSticker = (SimpleDraweeView) rootView.findViewById(R.id.iv_profile_nested_user_profile_for_sticker);
//        tvProfileNestedUserNameForSticker = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_user_name_for_sticker);
//        ivProfileNestedLineThroughForSticker = (ImageView) rootView.findViewById(R.id.iv_profile_nested_name_line_through_for_sticker);
//
//        ivProfileNestedCommentSticker = (SimpleDraweeView) rootView.findViewById(R.id.iv_profile_nested_comment_sticker);
//        tvProfileNestedCommentStickerCreateDate = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_sticker_create_date);
//        tvProfileNestedCommentStickerUnread = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_sticker_unread);
//
//    }
//
//    @Override
//    protected void initObjects() {
//        vgFileItem.setVisibility(View.GONE);
//        vgProfileNestedComment.setVisibility(View.GONE);
//        vgProfileNestedCommentSticker.setVisibility(View.VISIBLE);
//        tvPureComment.setVisibility(View.GONE);
//        vgPureCommentSticker.setVisibility(View.GONE);
//    }
//
//    @Override
//    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
//        super.bindData(link, teamId, roomId, entityId);
//
//        getStickerComment(link, teamId, roomId);
//
//        settingCommentUserInfo(link);
//    }
//
//    private void getStickerComment(ResMessages.Link link, long teamId, long roomId) {
//        ResMessages.CommentStickerMessage message = (ResMessages.CommentStickerMessage) link.message;
//
//        StickerManager.getInstance().loadStickerNoOption(ivProfileNestedCommentSticker, message.content.groupId, message.content.stickerId);
//
//        tvProfileNestedCommentStickerCreateDate.setText(DateTransformator.getTimeStringForSimple(message.createTime));
//        int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
//                link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());
//
//        if (unreadCount > 0) {
//            tvProfileNestedCommentStickerUnread.setText(String.valueOf(unreadCount));
//            tvProfileNestedCommentStickerUnread.setVisibility(View.VISIBLE);
//        } else {
//            tvProfileNestedCommentStickerUnread.setVisibility(View.GONE);
//        }
//    }
//
//    private void settingCommentUserInfo(ResMessages.Link link) {
//        long fromEntityId = link.fromEntity;
//        EntityManager entityManager = EntityManager.getInstance();
//        FormattedEntity entity = entityManager.getEntityById(fromEntityId);
//        ResLeftSideMenu.User fromEntity = entity.getUser();
//
//        String profileUrl = entity.getUserLargeProfileUrl();
//
//        ImageUtil.loadProfileImage(
//                ivProfileNestedUserProfileForSticker, profileUrl, R.drawable.profile_img);
//
//        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
//        ResLeftSideMenu.User user = entityById != EntityManager.UNKNOWN_USER_ENTITY ? entityById.getUser() : null;
//
//        if (user != null && entityById.isEnabled()) {
//            tvProfileNestedUserNameForSticker.setTextColor(
//                    JandiApplication.getContext().getResources().getColor(R.color.jandi_messages_name));
//            ivProfileNestedLineThroughForSticker.setVisibility(View.GONE);
//        } else {
//            tvProfileNestedUserNameForSticker.setTextColor(
//                    JandiApplication.getContext().getResources().getColor(R.color.deactivate_text_color));
//            ivProfileNestedLineThroughForSticker.setVisibility(View.VISIBLE);
//        }
//
//        tvProfileNestedUserNameForSticker.setText(fromEntity.name);
//
//        ivProfileNestedUserProfileForSticker.setOnClickListener(
//                v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Image)));
//
//        tvProfileNestedUserNameForSticker.setOnClickListener(
//                v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Name)));
//    }
//
//
//    public static class Builder {
//        private boolean hasBottomMargin = false;
//        private boolean hasSemiDivider = false;
//        private boolean hasCommentBubbleTail = false;
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
//        public ProfileWithStickerCommentViewHolder build() {
//            ProfileWithStickerCommentViewHolder viewHolder = new ProfileWithStickerCommentViewHolder();
//            viewHolder.setHasBottomMargin(hasBottomMargin);
//            viewHolder.setHasSemiDivider(hasSemiDivider);
//            viewHolder.setHasCommentBubbleTail(hasCommentBubbleTail);
//            return viewHolder;
//        }
//    }
//
//}
