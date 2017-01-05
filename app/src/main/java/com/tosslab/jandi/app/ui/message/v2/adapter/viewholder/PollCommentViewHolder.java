package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.dynamicl10n.FormatParam;
import com.tosslab.jandi.app.network.models.dynamicl10n.PollFinished;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.util.ProfileUtil;
import com.tosslab.jandi.app.ui.poll.util.PollBinder;
import com.tosslab.jandi.app.ui.poll.util.PollUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

import rx.android.schedulers.AndroidSchedulers;

public class PollCommentViewHolder extends BaseCommentViewHolder implements HighlightView {

    private ViewGroup vgPoll;
    private ImageView vPollIcon;
    private TextView tvSubject;
    private TextView tvCreator;
    private TextView tvDueDate;
    private TextView tvPollDeleted;

    private ImageView ivProfileNestedCommentUserProfile;
    private TextView tvProfileNestedCommentUserName;
    private ImageView ivProfileNestedNameLineThrough;
    private TextView tvProfileNestedCommentContent;
    private View vProfileCover;
    private ViewGroup vgProfileNestedComment;

    private Context context;

    private boolean hasNestedProfile = false;
    private boolean hasOnlyBadge = false;
    private boolean hasFlatTop = false;

    private PollCommentViewHolder() {
    }

    @Override
    public int getLayoutId() {
        if (hasNestedProfile) {
            return R.layout.item_comment_msg_v3;
        } else {
            return R.layout.item_comment_msg_collapse_v3;
        }
    }

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        if (hasContentInfo()) {
            stubContentInfo.setLayoutResource(R.layout.layout_comment_poll_info);
        }
        super.setOptionView();

        vgProfileNestedComment =
                (ViewGroup) rootView.findViewById(R.id.vg_profile_nested_comment);

        if (hasContentInfo()) {
            // Poll 정보
            vgPoll = (ViewGroup) rootView.findViewById(R.id.vg_message_poll);
            vPollIcon = (ImageView) rootView.findViewById(R.id.v_message_poll_icon);
            tvSubject = (TextView) rootView.findViewById(R.id.tv_message_poll_subject);
            tvCreator = (TextView) rootView.findViewById(R.id.tv_message_poll_creator);
            tvDueDate = (TextView) rootView.findViewById(R.id.tv_message_poll_due_date);
            tvPollDeleted = (TextView) rootView.findViewById(R.id.tv_message_poll_deleted);
        }

        // 프로필이 있는 커멘트
        if (hasNestedProfile) {
            ivProfileNestedCommentUserProfile = (ImageView) rootView.findViewById(R.id.iv_profile_nested_comment_user_profile);
            vProfileCover = rootView.findViewById(R.id.v_profile_nested_comment_user_profile_cover);
            tvProfileNestedCommentUserName = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_user_name);
            ivProfileNestedNameLineThrough = (ImageView) rootView.findViewById(R.id.iv_profile_nested_name_line_through);

            ivProfileNestedCommentUserProfile.setVisibility(View.VISIBLE);
            tvProfileNestedCommentUserName.setVisibility(View.VISIBLE);
            ivProfileNestedNameLineThrough.setVisibility(View.VISIBLE);
        }

        tvProfileNestedCommentContent = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_content);

        context = rootView.getContext();
    }

    @Override
    protected void initObjects() {
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        super.bindData(link, teamId, roomId, entityId);

        boolean hasContentInfo = hasContentInfo();
        if (hasContentInfo) {
            bindPoll(link);
            setPollInfoBackground(link);
        }

        if (hasNestedProfile) {
            ProfileUtil.setProfileForCommment(
                    link.fromEntity, ivProfileNestedCommentUserProfile, vProfileCover,
                    tvProfileNestedCommentUserName, ivProfileNestedNameLineThrough);
        }

        setCommentMessage(link, teamId, roomId);
        setBackground(link);

        if (hasCommentBubbleTail()) {
            // 파일 정보가 없고 내가 쓴 코멘트 인 경우만 comment_bubble_tail_mine resource 사
            vCommentBubbleTail.setBackgroundResource(hasContentInfo
                    ? R.drawable.bg_comment_bubble_tail :
                    isFromMe(link) ? R.drawable.comment_bubble_tail_mine : R.drawable.bg_comment_bubble_tail);
        }

    }

    private void setPollInfoBackground(ResMessages.Link link) {
        boolean isMe = isFromMe(link);
        if (isMe) {
            vgPoll.setBackgroundResource(R.drawable.bg_message_item_selector_mine);
        } else {
            vgPoll.setBackgroundResource(R.drawable.bg_message_item_selector);
        }
    }

    private boolean isFromMe(ResMessages.Link link) {
        boolean isMe = false;
        if (link.feedback != null) {
            isMe = TeamInfoLoader.getInstance().getMyId() == link.message.writerId;
        }
        return isMe;
    }

    private void setBackground(ResMessages.Link link) {

        boolean isMe = isFromMe(link);

        int resId;

        if (hasFlatTop) {
            if (hasBottomMargin) {
                if (isMe) {
                    resId = R.drawable.bg_message_item_selector_mine_flat_top;
                } else {
                    resId = R.drawable.bg_message_item_selector_flat_top;
                }
            } else {
                if (isMe) {
                    resId = R.drawable.bg_message_item_selector_mine_flat_all;
                } else {
                    resId = R.drawable.bg_message_item_selector_flat_all;
                }
            }
        } else {
            if (hasBottomMargin) {
                if (isMe) {
                    resId = R.drawable.bg_message_item_selector_mine;
                } else {
                    resId = R.drawable.bg_message_item_selector;

                }
            } else {

                if (isMe) {
                    resId = R.drawable.bg_message_item_selector_mine_flat_bottom;
                } else {
                    resId = R.drawable.bg_message_item_selector_flat_bottom;
                }
            }
        }
        vgProfileNestedComment.setBackgroundResource(resId);
    }

    private void setCommentMessage(ResMessages.Link link, long teamId, long roomId) {
        if (link.message instanceof ResMessages.CommentMessage) {
            ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) link.message;

            long myId = TeamInfoLoader.getInstance().getMyId();
            if (commentMessage.content.contentBuilder == null) {
                buildCommentMessage(commentMessage, myId);
            }

            SpannableStringBuilder builderWithBadge = new SpannableStringBuilder(commentMessage.content.contentBuilder);
            if (!hasOnlyBadge) {
                int startIndex = builderWithBadge.length();
                builderWithBadge.append(DateTransformator.getTimeStringForSimple(commentMessage.createTime));
                int endIndex = builderWithBadge.length();

                DateViewSpannable spannable =
                        new DateViewSpannable(tvProfileNestedCommentContent.getContext(),
                                DateTransformator.getTimeStringForSimple(commentMessage.createTime),
                                (int) UiUtils.getPixelFromSp(10f));
                spannable.setTextColor(
                        JandiApplication.getContext().getResources().getColor(R.color.jandi_messages_date));
                builderWithBadge.setSpan(spannable, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            UnreadCountUtil.getUnreadCount(roomId, link.id, link.fromEntity, myId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(unreadCount -> {
                        if (unreadCount > 0) {
                            NameSpannable unreadCountSpannable =
                                    new NameSpannable(
                                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9f, context.getResources().getDisplayMetrics())
                                            , context.getResources().getColor(R.color.jandi_accent_color));
                            int beforeLength = builderWithBadge.length();
                            builderWithBadge.append(" ");
                            builderWithBadge.append(String.valueOf(unreadCount))
                                    .setSpan(unreadCountSpannable, beforeLength, builderWithBadge.length(),
                                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        tvProfileNestedCommentContent.setText(builderWithBadge, TextView.BufferType.SPANNABLE);
                    });
        }
    }

    private void buildCommentMessage(ResMessages.CommentMessage commentMessage, long myId) {
        FormatParam formatMessage = commentMessage.formatMessage;
        if (formatMessage != null && formatMessage instanceof PollFinished) {

            commentMessage.content.contentBuilder =
                    PollUtil.buildFormatMessage(context, (PollFinished) formatMessage,
                            commentMessage, myId,
                            tvProfileNestedCommentContent.getTextSize());
        } else {
            SpannableStringBuilder messageBuilder = new SpannableStringBuilder();
            messageBuilder.append(!TextUtils.isEmpty(commentMessage.content.body) ? commentMessage.content.body : "");
            messageBuilder.append(" ");

            MentionAnalysisInfo mentionAnalysisInfo =
                    MentionAnalysisInfo.newBuilder(myId, commentMessage.mentions)
                            .textSize(tvProfileNestedCommentContent.getTextSize())
                            .clickable(true)
                            .build();

            SpannableLookUp.text(messageBuilder)
                    .hyperLink(false)
                    .markdown(false)
                    .webLink(false)
                    .emailLink(false)
                    .telLink(false)
                    .mention(mentionAnalysisInfo, false)
                    .lookUp(tvProfileNestedCommentContent.getContext());

            commentMessage.content.contentBuilder = messageBuilder;
        }
    }

    private void bindPoll(ResMessages.Link link) {
        PollBinder.bindPoll(link.poll, false,
                vPollIcon, tvSubject, tvCreator, tvDueDate, tvPollDeleted);
    }

    private void setHasNestedProfile(boolean hasNestedProfile) {
        this.hasNestedProfile = hasNestedProfile;
    }

    @Override
    public void setOnItemClickListener(final View.OnClickListener itemClickListener) {
        super.setOnItemClickListener(itemClickListener);

        View.OnClickListener onClickListenerWrapper = v -> {
            // TODO N개의 댓글 혹은 댓글이 press 됐을 때 화살표 색상바꿔줘야함...
            if (hasCommentBubbleTail()) {
            }

            itemClickListener.onClick(v);
        };

        if (vgPoll != null) {
            vgPoll.setOnClickListener(onClickListenerWrapper);
        }

        if (vgReadMore != null) {
            vgReadMore.setOnClickListener(onClickListenerWrapper);
        }

        vgProfileNestedComment.setOnClickListener(onClickListenerWrapper);
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        super.setOnItemLongClickListener(itemLongClickListener);

        View.OnLongClickListener onLongClickListenerWrapper = v -> {
            // TODO N개의 댓글 혹은 댓글이 press 됐을 때 화살표 색상바꿔줘야함...
            if (hasCommentBubbleTail()) {
            }
            return itemLongClickListener.onLongClick(v);
        };

        vgProfileNestedComment.setOnLongClickListener(onLongClickListenerWrapper);
    }

    public void setHasOnlyBadge(boolean hasOnlyBadge) {
        this.hasOnlyBadge = hasOnlyBadge;
    }

    protected void setHasFlatTop(boolean hasFlatTop) {
        this.hasFlatTop = hasFlatTop;
    }

    @Override
    public View getHighlightView() {
        return vgProfileNestedComment;
    }

    public static class Builder extends BaseViewHolderBuilder {

        public PollCommentViewHolder build() {
            PollCommentViewHolder viewHolder = new PollCommentViewHolder();
            viewHolder.setHasBottomMargin(hasBottomMargin);
            viewHolder.setHasSemiDivider(hasSemiDivider);
            viewHolder.setHasContentInfo(hasFileInfoView);
            viewHolder.setHasCommentBubbleTail(hasCommentBubbleTail);
            viewHolder.setHasNestedProfile(hasNestedProfile);
            viewHolder.setHasViewAllComment(hasViewAllComment);
            viewHolder.setHasOnlyBadge(hasOnlyBadge);
            viewHolder.setHasFlatTop(hasFlatTop);
            return viewHolder;
        }
    }

}
