package com.tosslab.jandi.app.lists.files.viewholder;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.utils.DateTransformator;

public class FileDetailCollapseStickerCommentView implements CommentViewHolder {

    SimpleDraweeView ivStickerContent;
    TextView tvCreatedTime;
    private View selectedView;

    @Override
    public void init(View rootView) {
        ivStickerContent = (SimpleDraweeView) rootView.findViewById(R.id.iv_file_detail_collapse_comment_content);
        tvCreatedTime = (TextView) rootView.findViewById(R.id.tv_file_detail_collapse_comment_create_date);
        selectedView = rootView.findViewById(R.id.v_file_detail_comment_anim);
    }

    @Override
    public void bind(ResMessages.OriginalMessage originalMessage) {

        ResMessages.CommentStickerMessage stickerMessage = (ResMessages.CommentStickerMessage) originalMessage;

        StickerManager.getInstance().loadStickerNoOption(ivStickerContent, stickerMessage.content.groupId, stickerMessage.content.stickerId);

        String createTime = DateTransformator.getTimeString(stickerMessage.createTime);
        tvCreatedTime.setText(createTime);

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
        return R.layout.item_file_detail_collapse_sticker_comment;
    }
}
