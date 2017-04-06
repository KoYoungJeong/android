package com.tosslab.jandi.app.ui.comment;

import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.ProfileBinder;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.SdkUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class StickerCommentViewHolder extends BaseViewHolder<ResMessages.CommentStickerMessage> implements CellDividerUpdater {
    @Nullable
    @Bind(R.id.tv_file_detail_comment_sticker_user_name)
    TextView tvUserName;
    @Nullable
    @Bind(R.id.iv_file_detail_comment_sticker_user_profile)
    ImageView ivUserProfile;
    @Nullable
    @Bind(R.id.v_file_detail_comment_sticker_user_name_disable_indicator)
    View vUserNameDisableIndicator;
    @Nullable
    @Bind(R.id.v_file_detail_comment_sticker_user_profile_disable_indicator)
    View vUserProfileDisableIndicator;
    @Bind(R.id.tv_file_detail_comment_sticker_create_date)
    TextView tvCreatedDate;
    @Bind(R.id.iv_file_detail_comment_sticker_content)
    ImageView ivSticker;
    @Bind(R.id.view_file_detail_comment_sticker_cell_divider)
    View vCellDivider;
    @Bind(R.id.v_file_detail_comment_sticker_background)
    View vBackground;

    private OnCommentClickListener onCommentClickListener;
    private OnCommentLongClickListener onCommentLongClickListener;
    private int defaultColor = 0;

    public StickerCommentViewHolder(View itemView,
                                    OnCommentClickListener onCommentClickListener,
                                    OnCommentLongClickListener onCommentLongClickListener) {
        super(itemView);
        this.onCommentClickListener = onCommentClickListener;
        this.onCommentLongClickListener = onCommentLongClickListener;
        ButterKnife.bind(this, itemView);

    }

    public static StickerCommentViewHolder newInstance(ViewGroup parent,
                                                       OnCommentClickListener onCommentClickListener,
                                                       OnCommentLongClickListener onCommentLongClickListener) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_file_detail_comment_sticker, parent, false);
        return new StickerCommentViewHolder(itemView, onCommentClickListener, onCommentLongClickListener);
    }

    public static StickerCommentViewHolder newInstanceNoProfile(ViewGroup parent,
                                                                OnCommentClickListener onCommentClickListener,
                                                                OnCommentLongClickListener onCommentLongClickListener) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_file_detail_comment_sticker_no_profile, parent, false);
        return new StickerCommentViewHolder(itemView, onCommentClickListener, onCommentLongClickListener);
    }

    @Override
    public void onBindView(ResMessages.CommentStickerMessage stickerMessage) {
        if (tvUserName != null) {
            User writer = TeamInfoLoader.getInstance().getUser(stickerMessage.writerId);
            ProfileBinder.newInstance(tvUserName, vUserNameDisableIndicator,
                    ivUserProfile, vUserProfileDisableIndicator)
                    .bindForComment(writer);
        }

        bindComment(stickerMessage);

        if (stickerMessage.writerId == TeamInfoLoader.getInstance().getMyId()) {
            if (defaultColor == 0) {
                defaultColor = vBackground.getResources().getColor(R.color.jandi_messages_blue_background);
            }
            vBackground.setBackgroundColor(defaultColor);
        } else {
            vBackground.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void bindComment(ResMessages.CommentStickerMessage commentStickerMessage) {
        // 날짜
        String createTime = DateTransformator.getTimeStringForComment(commentStickerMessage.createTime);
        tvCreatedDate.setText(createTime);

        // 댓글 내용
        ResMessages.StickerContent content = commentStickerMessage.content;
        StickerManager.getInstance().loadStickerNoOption(ivSticker, content.groupId, content.stickerId);

        itemView.setOnClickListener(v -> onCommentClickListener.onCommentClick(commentStickerMessage));
        itemView.setOnLongClickListener(v -> onCommentLongClickListener.onCommentLongClick(commentStickerMessage));
    }

    @Override
    public void cellUpdater(boolean full) {
        int rule;
        int parentRule;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) vCellDivider.getLayoutParams();
        if (SdkUtils.isOverJellyBeanMR1()) {
            if (lp.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR) {
                rule = RelativeLayout.ALIGN_LEFT;
                parentRule = RelativeLayout.ALIGN_PARENT_LEFT;
            } else {
                rule = RelativeLayout.ALIGN_RIGHT;
                parentRule = RelativeLayout.ALIGN_PARENT_RIGHT;
            }
        } else {
            rule = RelativeLayout.ALIGN_LEFT;
            parentRule = RelativeLayout.ALIGN_PARENT_LEFT;
        }

        if (full) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                lp.removeRule(rule);
            } else {
                lp.addRule(rule, 0);
            }
            lp.addRule(parentRule);
        } else {
            lp.addRule(rule, R.id.iv_file_detail_comment_sticker_content);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                lp.removeRule(parentRule);
            } else {
                lp.addRule(parentRule, 0);
            }
        }

        vCellDivider.setLayoutParams(lp);
    }
}
