//package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;
//
//import android.content.Context;
//import android.text.SpannableStringBuilder;
//import android.text.Spanned;
//import android.text.TextUtils;
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
//import com.tosslab.jandi.app.spannable.SpannableLookUp;
//import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
//import com.tosslab.jandi.app.utils.DateTransformator;
//import com.tosslab.jandi.app.utils.LinkifyUtil;
//import com.tosslab.jandi.app.utils.image.ImageUtil;
//import com.tosslab.jandi.app.views.spannable.DateViewSpannable;
//import com.tosslab.jandi.app.views.spannable.NameSpannable;
//
//import de.greenrobot.event.EventBus;
//
///**
// * Created by tee on 16. 4. 7..
// */
//public class ProfileWithCommentViewHolder extends BaseCommentViewHolder {
//
//    private SimpleDraweeView ivProfileNestedCommentUserProfile;
//    private TextView tvProfileNestedCommentUserName;
//    private ImageView ivProfileNestedNameLineThrough;
//    private TextView tvProfileNestedCommentContent;
//    private Context context;
//
//    @Override
//    public void initView(View rootView) {
//        super.initView(rootView);
//
//        // 프로필이 있는 커멘트
//        ivProfileNestedCommentUserProfile = (SimpleDraweeView) rootView.findViewById(R.id.iv_profile_nested_comment_user_profile);
//        tvProfileNestedCommentUserName = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_user_name);
//        ivProfileNestedNameLineThrough = (ImageView) rootView.findViewById(R.id.iv_profile_nested_name_line_through);
//        tvProfileNestedCommentContent = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_content);
//        context = rootView.getContext();
//    }
//
//    @Override
//    protected void initObjects() {
//        vgFileItem.setVisibility(View.GONE);
//        vgProfileNestedComment.setVisibility(View.VISIBLE);
//        vgProfileNestedCommentSticker.setVisibility(View.GONE);
//        tvPureComment.setVisibility(View.GONE);
//        vgPureCommentSticker.setVisibility(View.GONE);
//    }
//
//    @Override
//    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
//        super.bindData(link, teamId, roomId, entityId);
//
//        settingCommentMessage(link, teamId, roomId);
//        settingCommentUserInfo(link);
//    }
//
//    private void settingCommentMessage(ResMessages.Link link, long teamId, long roomId) {
//        if (link.message instanceof ResMessages.CommentMessage) {
//            ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) link.message;
//
//            SpannableStringBuilder builder = new SpannableStringBuilder();
//            builder.append(!TextUtils.isEmpty(commentMessage.content.body) ? commentMessage.content.body : "");
//            builder.append(" ");
//
//            long myId = EntityManager.getInstance().getMe().getId();
//            MentionAnalysisInfo mentionAnalysisInfo =
//                    MentionAnalysisInfo.newBuilder(myId, commentMessage.mentions)
//                            .textSizeFromResource(R.dimen.jandi_mention_message_view_comment_item_font_size)
//                            .build();
//
//            SpannableLookUp.text(builder)
//                    .hyperLink(false)
//                    .markdown(false)
//                    .webLink(false)
//                    .emailLink(false)
//                    .telLink(false)
//                    .mention(mentionAnalysisInfo, false)
//                    .lookUp(tvProfileNestedCommentContent.getContext());
//
//            LinkifyUtil.addLinks(context, builder);
//
//            int startIndex = builder.length();
//            builder.append(DateTransformator.getTimeStringForSimple(commentMessage.createTime));
//            int endIndex = builder.length();
//
//            DateViewSpannable spannable =
//                    new DateViewSpannable(tvProfileNestedCommentContent.getContext(),
//                            DateTransformator.getTimeStringForSimple(commentMessage.createTime));
//            spannable.setTextColor(
//                    JandiApplication.getContext().getResources().getColor(R.color.jandi_messages_date));
//            builder.setSpan(spannable, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//            int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
//                    link.id, link.fromEntity, myId);
//
//            if (unreadCount > 0) {
//                NameSpannable unreadCountSpannable =
//                        new NameSpannable(
//                                context.getResources().getDimensionPixelSize(R.dimen.jandi_comment_text_size)
//                                , context.getResources().getColor(R.color.jandi_accent_color));
//                int beforeLength = builder.length();
//                builder.append(" ");
//                builder.append(String.valueOf(unreadCount))
//                        .setSpan(unreadCountSpannable, beforeLength, builder.length(),
//                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            }
//
//            tvProfileNestedCommentContent.setText(builder);
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
//                ivProfileNestedCommentUserProfile, profileUrl, R.drawable.profile_img);
//
//        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
//        ResLeftSideMenu.User user = entityById != EntityManager.UNKNOWN_USER_ENTITY ? entityById.getUser() : null;
//
//        if (user != null && entityById.isEnabled()) {
//            tvProfileNestedCommentUserName.setTextColor(
//                    JandiApplication.getContext().getResources().getColor(R.color.jandi_messages_name));
//            ivProfileNestedNameLineThrough.setVisibility(View.GONE);
//        } else {
//            tvProfileNestedCommentUserName.setTextColor(
//                    JandiApplication.getContext().getResources().getColor(R.color.deactivate_text_color));
//            ivProfileNestedNameLineThrough.setVisibility(View.VISIBLE);
//        }
//
//        tvProfileNestedCommentUserName.setText(fromEntity.name);
//
//        ivProfileNestedCommentUserProfile.setOnClickListener(
//                v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Image)));
//
//        tvProfileNestedCommentUserName.setOnClickListener(
//                v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Name)));
//    }
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
//        public Builder setHasCommentBubbleTail(boolean hasCommentBubbleTail) {
//            this.hasCommentBubbleTail = hasCommentBubbleTail;
//            return this;
//        }
//
//        public ProfileWithCommentViewHolder build() {
//            ProfileWithCommentViewHolder viewHolder = new ProfileWithCommentViewHolder();
//            viewHolder.setHasBottomMargin(hasBottomMargin);
//            viewHolder.setHasSemiDivider(hasSemiDivider);
//            viewHolder.setHasCommentBubbleTail(hasCommentBubbleTail);
//            return viewHolder;
//        }
//    }
//
//}
