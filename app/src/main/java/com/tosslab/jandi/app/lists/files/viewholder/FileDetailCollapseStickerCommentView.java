package com.tosslab.jandi.app.lists.files.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.sticker.StickerManager;
import com.tosslab.jandi.app.utils.DateTransformator;

public class FileDetailCollapseStickerCommentView implements CommentViewHolder {

    ImageView ivStickerContent;
    TextView tvCreatedTime;

    @Override
    public void init(View rootView) {
        ivStickerContent = (ImageView) rootView.findViewById(R.id.iv_file_detail_collapse_comment_content);
        tvCreatedTime = (TextView) rootView.findViewById(R.id.tv_file_detail_collapse_comment_create_date);
    }

    @Override
    public void bind(ResMessages.OriginalMessage originalMessage) {

        ResMessages.CommentStickerMessage stickerMessage = (ResMessages.CommentStickerMessage) originalMessage;

        StickerManager.getInstance().loadStickerNoOption(ivStickerContent, stickerMessage.content.groupId, stickerMessage.content.stickerId);

        String createTime = DateTransformator.getTimeDifference(stickerMessage.createTime);
        tvCreatedTime.setText(createTime);

    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.item_file_detail_collapse_sticker_comment;
    }
}
