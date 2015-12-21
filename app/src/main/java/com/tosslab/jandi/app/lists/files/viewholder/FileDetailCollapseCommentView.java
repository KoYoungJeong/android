package com.tosslab.jandi.app.lists.files.viewholder;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.commonviewmodels.markdown.viewmodel.MarkdownViewModel;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.GenerateMentionMessageUtil;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;

public class FileDetailCollapseCommentView implements CommentViewHolder {

    TextView textViewCommentContent;
    private View selectedView;

    @Override
    public void init(View rootView) {
        textViewCommentContent = (TextView) rootView.findViewById(R.id.txt_file_detail_collapse_comment_content);
        selectedView = rootView.findViewById(R.id.v_file_detail_comment_anim);
    }

    @Override
    public void bind(ResMessages.OriginalMessage originalMessage) {

        ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) originalMessage;
        // 날짜
        String createTime = DateTransformator.getTimeString(commentMessage.createTime);
        // 댓글 내용
        String comment = commentMessage.content.body;

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(comment);

        boolean hasLink =
                LinkifyUtil.addLinks(textViewCommentContent.getContext(), spannableStringBuilder);

        if (hasLink) {
            Spannable linkSpannable =
                    Spannable.Factory.getInstance().newSpannable(spannableStringBuilder);
            spannableStringBuilder.setSpan(linkSpannable,
                    0, comment.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            LinkifyUtil.setOnLinkClick(textViewCommentContent);
        }

        spannableStringBuilder.append(" ");

        Resources resources = textViewCommentContent.getResources();

        int dateSpannableTextSize = ((int) resources.getDimension(R.dimen.jandi_messages_date));
        int dateSpannableTextColor = resources.getColor(R.color.jandi_messages_date);

        int startIndex = spannableStringBuilder.length();
        spannableStringBuilder.append(" ");
        int endIndex = spannableStringBuilder.length();

        DateViewSpannable spannable =
                new DateViewSpannable(textViewCommentContent.getContext(), createTime);
        spannableStringBuilder.setSpan(spannable,
                startIndex, endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        MarkdownViewModel markdownViewModel = new MarkdownViewModel(textViewCommentContent, spannableStringBuilder, false);
        markdownViewModel.execute();

        GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
                textViewCommentContent, spannableStringBuilder, commentMessage.mentions,
                EntityManager.getInstance().getMe().getId())
                .setPxSize(R.dimen.jandi_mention_comment_item_font_size);
        spannableStringBuilder = generateMentionMessageUtil.generate(true);

        textViewCommentContent.setText(spannableStringBuilder);
    }

    @Override
    public void startAnimation(Animator.AnimatorListener animatorListener) {

        Context context = selectedView.getContext();

        Integer colorFrom = context.getResources().getColor(R.color.transparent);
        Integer colorTo = context.getResources().getColor(R.color.jandi_accent_color_1f);
        final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(context.getResources().getInteger(R.integer.highlight_animation_time));
        colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
        colorAnimation.setRepeatCount(1);
        colorAnimation.addUpdateListener(animator -> selectedView.setBackgroundColor((Integer)
                animator.getAnimatedValue()));

        colorAnimation.addListener(animatorListener);
        colorAnimation.start();
    }


    @Override
    public int getLayoutResourceId() {
        return R.layout.item_file_detail_collapse_comment;
    }
}
