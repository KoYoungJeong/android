package com.tosslab.jandi.app.lists.files.viewholder;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
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

    @Override
    public void init(View rootView) {
        imageViewCommentUserProfile = (ImageView) rootView.findViewById(R.id.img_file_detail_comment_user_profile);
        textViewCommentUserName = (TextView) rootView.findViewById(R.id.txt_file_detail_comment_user_name);
        textViewCommentFileCreateDate = (TextView) rootView.findViewById(R.id.txt_file_detail_comment_create_date);
        ivCommentSticker = (ImageView) rootView.findViewById(R.id.iv_file_detail_comment_sticker);
        disableLineThrougView = rootView.findViewById(R.id.img_entity_listitem_line_through);
        disableCoverView = rootView.findViewById(R.id.view_entity_listitem_warning);
    }

    @Override
    public void bind(ResMessages.OriginalMessage originalMessage) {
        ResMessages.CommentStickerMessage commentMessage = (ResMessages.CommentStickerMessage) originalMessage;

        // 프로필
        final FormattedEntity writer = EntityManager.getInstance(imageViewCommentUserProfile.getContext()).getEntityById(commentMessage.writerId);

        String profileUrl = writer.getUserSmallProfileUrl();
        EntityManager entityManager = EntityManager.getInstance(imageViewCommentUserProfile.getContext());
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
                .placeholder(R.drawable.jandi_profile_comment)
                .error(R.drawable.jandi_profile_comment)
                .transform(new IonCircleTransform())
                .load(profileUrl);

        imageViewCommentUserProfile.setOnClickListener(view -> EventBus.getDefault().post(new RequestUserInfoEvent(writer.getId())));
        // 이름
        String userName = writer.getName();
        textViewCommentUserName.setText(userName);
        // 날짜
        String createTime = DateTransformator.getTimeDifference(commentMessage.createTime);
        textViewCommentFileCreateDate.setText(createTime);

        ResMessages.StickerContent stickerContent = commentMessage.content;

        StickerManager.getInstance().loadStickerNoOption(ivCommentSticker, stickerContent.groupId, stickerContent.stickerId);

    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.item_file_detail_comment_sticker;
    }
}
