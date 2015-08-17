package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.sticker.StickerManager;
import com.tosslab.jandi.app.utils.DateTransformator;

/**
 * Created by Steve SeongUg Jung on 15. 6. 9..
 */
public class CollapseStickerCommentViewHolder implements BodyViewHolder {

    private ImageView ivSticker;
    private TextView tvCreatedTime;
    private TextView tvUnreadCount;
    private View lastReadView;
    private View contentView;

    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_message_item);
        ivSticker = (ImageView) rootView.findViewById(R.id.iv_pure_sticker_comment_content);
        tvCreatedTime = (TextView) rootView.findViewById(R.id.tv_pure_sticker_comment_create_date);
        tvUnreadCount = (TextView) rootView.findViewById(R.id.tv_pure_sticker_comment_unread);
        lastReadView = rootView.findViewById(R.id.vg_message_last_read);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {
        ResMessages.CommentStickerMessage message = (ResMessages.CommentStickerMessage) link.message;

        StickerManager.getInstance().loadStickerNoOption(ivSticker, message.content.groupId, message.content.stickerId);

        tvCreatedTime.setText(DateTransformator.getTimeStringForSimple(message.createTime));
        int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
                link.id, link.fromEntity, EntityManager.getInstance(tvUnreadCount.getContext()).getMe().getId());

        if (unreadCount > 0) {
            tvUnreadCount.setText(String.valueOf(unreadCount));
            tvUnreadCount.setVisibility(View.VISIBLE);
        } else {
            tvUnreadCount.setVisibility(View.GONE);
        }
    }

    @Override
    public void setLastReadViewVisible(int currentLinkId, int lastReadLinkId) {
        if (currentLinkId == lastReadLinkId) {
            lastReadView.setVisibility(View.VISIBLE);
        } else {
            lastReadView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_pure_sticker_cmt_without_file_v2;
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        if (contentView != null && itemClickListener != null) {
            contentView.setOnClickListener(itemClickListener);
        }
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        if (contentView != null && itemLongClickListener != null) {
            contentView.setOnLongClickListener(itemLongClickListener);
        }
    }
}
