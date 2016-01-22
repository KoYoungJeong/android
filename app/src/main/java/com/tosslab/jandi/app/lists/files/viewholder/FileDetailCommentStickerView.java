package com.tosslab.jandi.app.lists.files.viewholder;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 6. 9..
 */
public class FileDetailCommentStickerView implements CommentViewHolder {

    SimpleDraweeView ivCommentUserProfile;
    TextView tvCommentUserName;
    TextView tvCommentFileCreateDate;
    SimpleDraweeView ivCommentSticker;

    View vDisableLineThrough;
    View vDisableCover;
    private View selectedView;

    @Override
    public void init(View rootView) {
        ivCommentUserProfile =
                (SimpleDraweeView) rootView.findViewById(R.id.iv_file_detail_comment_user_profile);
        tvCommentUserName = (TextView) rootView.findViewById(R.id.tv_file_detail_comment_user_name);
        tvCommentFileCreateDate = (TextView) rootView.findViewById(R.id.tv_file_detail_comment_create_date);
        ivCommentSticker = (SimpleDraweeView) rootView.findViewById(R.id.iv_file_detail_comment_sticker_content);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);
        vDisableCover = rootView.findViewById(R.id.v_entity_listitem_warning);
        selectedView = rootView.findViewById(R.id.v_file_detail_comment_anim);
    }

    @Override
    public void bind(ResMessages.OriginalMessage originalMessage) {
        ResMessages.CommentStickerMessage commentMessage = (ResMessages.CommentStickerMessage) originalMessage;

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
            tvCommentUserName.setTextColor(
                    tvCommentUserName.getContext().getResources().getColor(R.color.deactivate_text_color));
        }

        ImageUtil.loadProfileImage(
                ivCommentUserProfile, profileUrl, R.drawable.profile_img_comment);

        ivCommentUserProfile.setOnClickListener(v -> {
            EventBus.getDefault().post(new ShowProfileEvent(writer.getId(), ShowProfileEvent.From.Image));
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewProfile_FromComment);
        });
        // 이름
        String userName = writer.getName();
        tvCommentUserName.setText(userName);
        // 날짜
        String createTime = DateTransformator.getTimeString(commentMessage.createTime);
        tvCommentFileCreateDate.setText(createTime);

        ResMessages.StickerContent stickerContent = commentMessage.content;

        StickerManager.getInstance()
                .loadStickerNoOption(ivCommentSticker, stickerContent.groupId, stickerContent.stickerId);

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
        return R.layout.item_file_detail_comment_sticker;
    }
}
