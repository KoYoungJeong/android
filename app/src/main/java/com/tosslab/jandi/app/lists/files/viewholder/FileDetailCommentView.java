package com.tosslab.jandi.app.lists.files.viewholder;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.ui.commonviewmodels.markdown.viewmodel.MarkdownViewModel;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.GenerateMentionMessageUtil;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 19..
 */
public class FileDetailCommentView implements CommentViewHolder {

    SimpleDraweeView ivCommentUserProfile;
    TextView tvCommentUserName;
    TextView tvCommentContent;

    View vDisableLineThrough;
    View vDisableCover;

    View selectedView;

    @Override
    public void init(View rootView) {
        ivCommentUserProfile =
                (SimpleDraweeView) rootView.findViewById(R.id.iv_file_detail_comment_user_profile);
        tvCommentUserName = (TextView) rootView.findViewById(R.id.tv_file_detail_comment_user_name);
        tvCommentContent = (TextView) rootView.findViewById(R.id.txt_file_detail_comment_content_2);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);
        vDisableCover = rootView.findViewById(R.id.v_entity_listitem_warning);
        selectedView = rootView.findViewById(R.id.v_file_detail_comment_anim);
    }

    @Override
    public void bind(ResMessages.OriginalMessage originalMessage) {

        ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) originalMessage;

        // 프로필
        final FormattedEntity writer = EntityManager.getInstance().getEntityById(commentMessage.writerId);

        String profileUrl = writer.getUserSmallProfileUrl();
        EntityManager entityManager = EntityManager.getInstance();
        if (TextUtils.equals(entityManager.getEntityById(commentMessage.writerId).getUser().status, "enabled")) {
            vDisableLineThrough.setVisibility(View.GONE);
            vDisableCover.setVisibility(View.GONE);
            tvCommentUserName.setTextColor(Color.BLACK);
        } else {
            vDisableLineThrough.setVisibility(View.VISIBLE);
            vDisableCover.setVisibility(View.VISIBLE);
            tvCommentUserName.setTextColor(tvCommentUserName.getContext().getResources().getColor(R.color.deactivate_text_color));
        }

        ImageUtil.loadCircleImageByFresco(
                ivCommentUserProfile, profileUrl, R.drawable.profile_img_comment);

        ivCommentUserProfile.setOnClickListener(v ->
                EventBus.getDefault().post(new ShowProfileEvent(writer.getId(), ShowProfileEvent.From.Image)));
        // 이름
        String userName = writer.getName();
        tvCommentUserName.setText(userName);
        // 날짜
        String createTime = DateTransformator.getTimeString(commentMessage.createTime);
        // 댓글 내용
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(commentMessage.content.body);

        boolean hasLink = LinkifyUtil.addLinks(tvCommentContent.getContext(), spannableStringBuilder);

        if (hasLink) {
            Spannable linkSpannable = Spannable.Factory.getInstance().newSpannable(spannableStringBuilder);
            spannableStringBuilder.setSpan(linkSpannable, 0, commentMessage.content.body.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            LinkifyUtil.setOnLinkClick(tvCommentContent);
        }

        spannableStringBuilder.append(" ");

        Resources resources = ivCommentUserProfile.getContext().getResources();
        //날짜
        int dateSpannableTextSize = ((int) resources.getDimension(R.dimen.jandi_messages_date));
        int dateSpannableTextColor = resources.getColor(R.color.jandi_messages_date);

        int startIndex = spannableStringBuilder.length();
        spannableStringBuilder.append(" ");
        int endIndex = spannableStringBuilder.length();

        DateViewSpannable spannable =
                new DateViewSpannable(tvCommentContent.getContext(), createTime);
        spannableStringBuilder.setSpan(spannable,
                startIndex, endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        MarkdownViewModel markdownViewModel = new MarkdownViewModel(tvCommentContent, spannableStringBuilder, false);
        markdownViewModel.execute();

        GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
                tvCommentContent, spannableStringBuilder, commentMessage.mentions, entityManager.getMe().getId())
                .setPxSize(R.dimen.jandi_mention_comment_item_font_size);
        spannableStringBuilder = generateMentionMessageUtil.generate(true);

        tvCommentContent.setText(spannableStringBuilder);
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
        return R.layout.item_file_detail_comment;
    }
}
