package com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.comment;

import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.ProfileBinder;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;

/**
 * Created by tonyjs on 16. 1. 28..
 */
public class CommentViewHolder extends BaseViewHolder<ResMessages.CommentMessage> {
    private TextView tvUserName;
    private ImageView ivUserProfile;
    private View vUserNameDisableIndicator;
    private View vUserProfileDisableIndicator;
    private TextView tvCreatedDate;
    private TextView tvCommentContent;

    public CommentViewHolder(View itemView) {
        super(itemView);

        tvUserName = (TextView) itemView.findViewById(R.id.tv_file_detail_comment_user_name);
        ivUserProfile = (ImageView) itemView.findViewById(R.id.iv_file_detail_comment_user_profile);
        tvCreatedDate = (TextView) itemView.findViewById(R.id.tv_file_detail_comment_create_date);

        vUserNameDisableIndicator =
                itemView.findViewById(R.id.v_file_detail_comment_user_name_disable_indicator);
        vUserProfileDisableIndicator =
                itemView.findViewById(R.id.v_file_detail_comment_user_profile_disable_indicator);

        tvCommentContent = (TextView) itemView.findViewById(R.id.tv_file_detail_comment_content);
    }

    public static CommentViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_file_detail_comment, parent, false);
        return new CommentViewHolder(itemView);
    }

    @Override
    public void onBindView(ResMessages.CommentMessage commentMessage) {
        User writer = TeamInfoLoader.getInstance().getUser(commentMessage.writerId);
        ProfileBinder.newInstance(tvUserName, vUserNameDisableIndicator,
                ivUserProfile, vUserProfileDisableIndicator)
                .bindForComment(writer);

        bindComment(commentMessage);
    }

    public void bindComment(ResMessages.CommentMessage commentMessage) {
        // 날짜
        String createTime = DateTransformator.getTimeString(commentMessage.createTime);
        tvCreatedDate.setText(createTime);

        // 댓글 내용
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(commentMessage.content.body);

        long myId = TeamInfoLoader.getInstance().getMyId();

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

        LinkifyUtil.setOnLinkClick(tvCommentContent);

        tvCommentContent.setText(spannableStringBuilder, TextView.BufferType.SPANNABLE);
    }
}
