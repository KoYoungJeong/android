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
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.GenerateMentionMessageUtil;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 19..
 */
public class FileDetailCommentView implements CommentViewHolder {

    ImageView imageViewCommentUserProfile;
    TextView textViewCommentUserName;
    TextView textViewCommentContent;

    View disableLineThrougView;

    View disableCoverView;

    View selectedView;

    @Override
    public void init(View rootView) {
        imageViewCommentUserProfile = (ImageView) rootView.findViewById(R.id.img_file_detail_comment_user_profile);
        textViewCommentUserName = (TextView) rootView.findViewById(R.id.txt_file_detail_comment_user_name);
        textViewCommentContent = (TextView) rootView.findViewById(R.id.txt_file_detail_comment_content_2);
        disableLineThrougView = rootView.findViewById(R.id.img_entity_listitem_line_through);
        disableCoverView = rootView.findViewById(R.id.view_entity_listitem_warning);
        selectedView = rootView.findViewById(R.id.view_file_detail_comment_anim);
    }

    @Override
    public void bind(ResMessages.OriginalMessage originalMessage) {

        ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) originalMessage;

        // 프로필
        final FormattedEntity writer = EntityManager.getInstance().getEntityById(commentMessage.writerId);

        String profileUrl = writer.getUserSmallProfileUrl();
        EntityManager entityManager = EntityManager.getInstance();
        if (TextUtils.equals(entityManager.getEntityById(commentMessage.writerId).getUser().status, "enabled")) {
            disableLineThrougView.setVisibility(View.GONE);
            disableCoverView.setVisibility(View.GONE);
            textViewCommentUserName.setTextColor(Color.BLACK);
        } else {
            disableLineThrougView.setVisibility(View.VISIBLE);
            disableCoverView.setVisibility(View.VISIBLE);
            textViewCommentUserName.setTextColor(textViewCommentUserName.getContext().getResources().getColor(R.color.deactivate_text_color));
        }

        Ion.with(imageViewCommentUserProfile)
                .placeholder(R.drawable.profile_img_comment)
                .error(R.drawable.profile_img_comment)
                .transform(new IonCircleTransform())
                .load(profileUrl);

        imageViewCommentUserProfile.setOnClickListener(view -> EventBus.getDefault().post(new RequestUserInfoEvent(writer.getId())));
        // 이름
        String userName = writer.getName();
        textViewCommentUserName.setText(userName);
        // 날짜
        String createTime = DateTransformator.getTimeString(commentMessage.createTime);
        // 댓글 내용
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(commentMessage.content.body);

        boolean hasLink = LinkifyUtil.addLinks(textViewCommentContent.getContext(), spannableStringBuilder);

        if (hasLink) {
            Spannable linkSpannable = Spannable.Factory.getInstance().newSpannable(spannableStringBuilder);
            spannableStringBuilder.setSpan(linkSpannable, 0, commentMessage.content.body.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            LinkifyUtil.setOnLinkClick(textViewCommentContent);
        }

        spannableStringBuilder.append(" ");

        Resources resources = imageViewCommentUserProfile.getContext().getResources();
        //날짜
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

        GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
                textViewCommentContent, spannableStringBuilder, commentMessage.mentions, entityManager.getMe().getId())
                .setPxSize(R.dimen.jandi_mention_comment_item_font_size);
        spannableStringBuilder = generateMentionMessageUtil.generate(true);

        textViewCommentContent.setText(spannableStringBuilder);
    }

    @Override
    public void startAnimation(Animator.AnimatorListener animatorListener) {
        Context context = selectedView.getContext();

        Integer colorFrom = context.getResources().getColor(R.color.transparent);
        Integer colorTo = context.getResources().getColor(R.color.jandi_accent_color_50);
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
