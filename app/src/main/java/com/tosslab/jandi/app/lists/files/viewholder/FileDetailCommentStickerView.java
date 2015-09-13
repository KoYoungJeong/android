package com.tosslab.jandi.app.lists.files.viewholder;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
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
import com.tosslab.jandi.app.ui.sticker.StickerManager;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.IonCircleTransform;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 6. 9..
 */
public class FileDetailCommentStickerView implements CommentViewHolder {

    ImageView imageViewCommentUserProfile;
    TextView textViewCommentUserName;
    TextView textViewCommentFileCreateDate;
    ImageView ivCommentSticker;

    View disableLineThrougView;

    View disableCoverView;
    private View selectedView;

    @Override
    public void init(View rootView) {
        imageViewCommentUserProfile = (ImageView) rootView.findViewById(R.id.img_file_detail_comment_user_profile);
        textViewCommentUserName = (TextView) rootView.findViewById(R.id.txt_file_detail_comment_user_name);
        textViewCommentFileCreateDate = (TextView) rootView.findViewById(R.id.txt_file_detail_comment_create_date);
        ivCommentSticker = (ImageView) rootView.findViewById(R.id.iv_file_detail_comment_sticker);
        disableLineThrougView = rootView.findViewById(R.id.iv_entity_listitem_line_through);
        disableCoverView = rootView.findViewById(R.id.view_entity_listitem_warning);
        selectedView = rootView.findViewById(R.id.view_file_detail_comment_anim);
    }

    @Override
    public void bind(ResMessages.OriginalMessage originalMessage) {
        ResMessages.CommentStickerMessage commentMessage = (ResMessages.CommentStickerMessage) originalMessage;

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
        textViewCommentFileCreateDate.setText(createTime);

        ResMessages.StickerContent stickerContent = commentMessage.content;

        StickerManager.getInstance().loadStickerNoOption(ivCommentSticker, stickerContent.groupId, stickerContent.stickerId);

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
        return R.layout.item_file_detail_comment_sticker;
    }
}
