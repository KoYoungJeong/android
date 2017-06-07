package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.util.ProfileUtil;
import com.tosslab.jandi.app.ui.poll.util.PollBinder;
import com.tosslab.jandi.app.utils.DateTransformator;

public class PollStickerCommentViewHolder extends BaseCommentViewHolder implements HighlightView {

    private ViewGroup vgPoll;
    private ImageView vPollIcon;
    private TextView tvSubject;
    private TextView tvCreator;
    private TextView tvDueDate;
    private TextView tvPollDeleted;

    private ImageView ivProfileNestedUserProfileForSticker;
    private ViewGroup vgProfileAbsence;
    private TextView tvProfileNestedUserNameForSticker;
    private ImageView ivProfileNestedLineThroughForSticker;

    private ImageView ivProfileNestedCommentSticker;
    private TextView tvProfileNestedCommentStickerCreateDate;
    private TextView tvProfileNestedCommentStickerUnread;

    private View vProfileCover;
    private ViewGroup vgProfileNestedCommentSticker;

    private Context context;

    private boolean hasNestedProfile = false;
    private boolean hasOnlyBadge;
    private boolean hasFlatTop = false;

    private PollStickerCommentViewHolder() {
    }

    @Override
    public int getLayoutId() {
        if (hasNestedProfile) {
            return R.layout.item_comment_sticker_v3;
        } else {
            return R.layout.item_comment_sticker_collapse_v3;
        }
    }

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        if (hasContentInfo()) {
            stubContentInfo.setLayoutResource(R.layout.layout_comment_poll_info);
        }
        super.setOptionView();

        vgProfileNestedCommentSticker =
                (ViewGroup) rootView.findViewById(R.id.vg_profile_nested_comment_sticker);

        if (hasContentInfo()) {
            // Poll 정보
            vgPoll = (ViewGroup) rootView.findViewById(R.id.vg_message_poll);
            vPollIcon = (ImageView) rootView.findViewById(R.id.v_message_poll_icon);
            tvSubject = (TextView) rootView.findViewById(R.id.tv_message_poll_subject);
            tvCreator = (TextView) rootView.findViewById(R.id.tv_message_poll_creator);
            tvDueDate = (TextView) rootView.findViewById(R.id.tv_message_poll_due_date);
            tvPollDeleted = (TextView) rootView.findViewById(R.id.tv_message_poll_deleted);
        }

        // 커멘트 스티커 프로필
        if (hasNestedProfile) {
            ivProfileNestedUserProfileForSticker = (ImageView) rootView.findViewById(R.id.iv_profile_nested_user_profile_for_sticker);
            vgProfileAbsence = (ViewGroup) rootView.findViewById(R.id.vg_profile_absence);
            vProfileCover = rootView.findViewById(R.id.v_profile_nested_user_profile_for_sticker_cover);
            tvProfileNestedUserNameForSticker = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_user_name_for_sticker);
            ivProfileNestedLineThroughForSticker = (ImageView) rootView.findViewById(R.id.iv_profile_nested_name_line_through_for_sticker);
        }

        // 스티커
        ivProfileNestedCommentSticker = (ImageView) rootView.findViewById(R.id.iv_profile_nested_comment_sticker);
        tvProfileNestedCommentStickerCreateDate = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_sticker_create_date);
        tvProfileNestedCommentStickerUnread = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_sticker_unread);
        context = rootView.getContext();

        if (hasNestedProfile) {
            ivProfileNestedUserProfileForSticker.setVisibility(View.VISIBLE);
            tvProfileNestedUserNameForSticker.setVisibility(View.VISIBLE);
        }
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
                    link.fromEntity, ivProfileNestedUserProfileForSticker, vgProfileAbsence, vProfileCover,
                    tvProfileNestedUserNameForSticker, ivProfileNestedLineThroughForSticker);
        }

        getStickerComment(link, teamId, roomId);
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

        vgProfileNestedCommentSticker.setBackgroundResource(resId);
    }

    private void getStickerComment(ResMessages.Link link, long teamId, long roomId) {
        ResMessages.CommentStickerMessage message = (ResMessages.CommentStickerMessage) link.message;

        StickerManager.getInstance().loadStickerNoOption(ivProfileNestedCommentSticker, message.content.groupId, message.content.stickerId);

        if (hasOnlyBadge) {
            tvProfileNestedCommentStickerCreateDate.setVisibility(View.GONE);
        } else {
            tvProfileNestedCommentStickerCreateDate.setVisibility(View.VISIBLE);
            tvProfileNestedCommentStickerCreateDate.setText(DateTransformator.getTimeStringForSimple(message.createTime));

        }

        if (link.unreadCnt > 0) {
            tvProfileNestedCommentStickerUnread.setText(String.valueOf(link.unreadCnt));
            tvProfileNestedCommentStickerUnread.setVisibility(View.VISIBLE);
        } else {
            tvProfileNestedCommentStickerUnread.setVisibility(View.GONE);
        }

    }

    private void bindPoll(ResMessages.Link link) {
        PollBinder.bindPoll(link.poll, false,
                vPollIcon, tvSubject, tvCreator, tvDueDate, tvPollDeleted);
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        super.setOnItemClickListener(itemClickListener);
        if (vgPoll != null) {
            vgPoll.setOnClickListener(itemClickListener);
        }
        if (vgReadMore != null) {
            vgReadMore.setOnClickListener(itemClickListener);
        }
        vgProfileNestedCommentSticker.setOnClickListener(itemClickListener);
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        super.setOnItemLongClickListener(itemLongClickListener);
        vgProfileNestedCommentSticker.setOnLongClickListener(itemLongClickListener);
    }

    private void setHasNestedProfile(boolean hasNestedProfile) {
        this.hasNestedProfile = hasNestedProfile;
    }

    public void setHasOnlyBadge(boolean hasOnlyBadge) {
        this.hasOnlyBadge = hasOnlyBadge;
    }

    public void setHasFlatTop(boolean hasFlatTop) {
        this.hasFlatTop = hasFlatTop;
    }

    @Override
    public View getHighlightView() {
        return vgProfileNestedCommentSticker;
    }


    public static class Builder extends BaseViewHolderBuilder {

        public PollStickerCommentViewHolder build() {
            PollStickerCommentViewHolder viewHolder = new PollStickerCommentViewHolder();
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
