package com.tosslab.jandi.app.ui.comment;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.dynamicl10n.FormatParam;
import com.tosslab.jandi.app.network.models.dynamicl10n.PollFinished;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.ProfileBinder;
import com.tosslab.jandi.app.ui.poll.util.PollUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.SdkUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TextCommentViewHolder extends BaseViewHolder<ResMessages.CommentMessage> implements CellDividerUpdater {

    @Nullable
    @Bind(R.id.tv_file_detail_comment_user_name)
    TextView tvUserName;
    @Nullable
    @Bind(R.id.iv_file_detail_comment_user_profile)
    ImageView ivUserProfile;
    @Nullable
    @Bind(R.id.v_file_detail_comment_user_name_disable_indicator)
    View vUserNameDisableIndicator;
    @Nullable
    @Bind(R.id.v_file_detail_comment_user_profile_disable_indicator)
    View vUserProfileDisableIndicator;
    @Bind(R.id.tv_file_detail_comment_create_date)
    TextView tvCreatedDate;
    @Bind(R.id.tv_file_detail_comment_content)
    TextView tvCommentContent;
    @Bind(R.id.view_file_detail_comment_cell_divider)
    View vCellDivider;
    @Bind(R.id.v_file_detail_comment_background)
    View vBackground;

    private OnCommentClickListener onCommentClickListener;
    private OnCommentLongClickListener onCommentLongClickListener;
    private int defaultColor = 0;

    public TextCommentViewHolder(View itemView,
                                 OnCommentClickListener onCommentClickListener,
                                 OnCommentLongClickListener onCommentLongClickListener) {
        super(itemView);
        this.onCommentClickListener = onCommentClickListener;
        this.onCommentLongClickListener = onCommentLongClickListener;
        ButterKnife.bind(this, itemView);

    }

    public static TextCommentViewHolder newInstance(ViewGroup parent,
                                                    OnCommentClickListener onCommentClickListener,
                                                    OnCommentLongClickListener onCommentLongClickListener) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_file_detail_comment, parent, false);
        return new TextCommentViewHolder(itemView, onCommentClickListener, onCommentLongClickListener);
    }

    public static TextCommentViewHolder newInstanceNoProfile(ViewGroup parent,
                                                             OnCommentClickListener onCommentClickListener,
                                                             OnCommentLongClickListener onCommentLongClickListener) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_file_detail_comment_no_profile, parent, false);
        return new TextCommentViewHolder(itemView, onCommentClickListener, onCommentLongClickListener);
    }

    @Override
    public void onBindView(ResMessages.CommentMessage commentMessage) {
        if (tvUserName != null) {
            User writer = TeamInfoLoader.getInstance().getUser(commentMessage.writerId);
            ProfileBinder.newInstance(tvUserName,
                    vUserNameDisableIndicator,
                    ivUserProfile,
                    vUserProfileDisableIndicator).bindForComment(writer);
        }

        bindComment(commentMessage);

        if (commentMessage.writerId == TeamInfoLoader.getInstance().getMyId()) {
            if (defaultColor == 0) {
                defaultColor = vBackground.getResources().getColor(R.color.jandi_messages_blue_background);
            }
            vBackground.setBackgroundColor(defaultColor);
        } else {
            vBackground.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void bindComment(ResMessages.CommentMessage commentMessage) {
        // 날짜
        String createTime = DateTransformator.getTimeStringForComment(commentMessage.createTime);
        tvCreatedDate.setText(createTime);

        long myId = TeamInfoLoader.getInstance().getMyId();

        if (commentMessage.content.contentBuilder == null) {
            // 댓글 내용
            FormatParam formatMessage = commentMessage.formatMessage;
            if (formatMessage != null && formatMessage instanceof PollFinished) {

                commentMessage.content.contentBuilder =
                        PollUtil.buildFormatMessage(
                                tvCommentContent.getContext(), (PollFinished) formatMessage,
                                commentMessage, myId,
                                tvCommentContent.getTextSize());
            } else {
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                spannableStringBuilder.append(commentMessage.content.body);

                MentionAnalysisInfo mentionAnalysisInfo =
                        MentionAnalysisInfo.newBuilder(myId, commentMessage.mentions)
                                .textSizeFromResource(R.dimen.jandi_mention_comment_item_font_size)
                                .build();

                SpannableLookUp.text(spannableStringBuilder)
                        .hyperLink(false)
                        .webLink(false)
                        .emailLink(false)
                        .telLink(false)
                        .markdown(false)
                        .mention(mentionAnalysisInfo, false)
                        .lookUp(tvCommentContent.getContext());

                commentMessage.content.contentBuilder = spannableStringBuilder;
            }
        }

        LinkifyUtil.setOnLinkClick(tvCommentContent);
        tvCommentContent.setText(commentMessage.content.contentBuilder, TextView.BufferType.SPANNABLE);

        itemView.setOnClickListener(v -> onCommentClickListener.onCommentClick(commentMessage));
        itemView.setOnLongClickListener(v -> onCommentLongClickListener.onCommentLongClick(commentMessage));
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

            lp.addRule(rule, R.id.tv_file_detail_comment_content);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                lp.removeRule(parentRule);
            } else {
                lp.addRule(parentRule, 0);
            }
        }

        vCellDivider.setLayoutParams(lp);
    }
}
