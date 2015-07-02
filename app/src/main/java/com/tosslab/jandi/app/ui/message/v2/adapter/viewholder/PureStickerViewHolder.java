package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.sticker.StickerManager;
import com.tosslab.jandi.app.utils.DateTransformator;

/**
 * Created by Steve SeongUg Jung on 15. 5. 6..
 */
public class PureStickerViewHolder implements BodyViewHolder {

    private ImageView ivSticker;
    private TextView tvDate;
    private TextView tvUnread;

    @Override
    public void initView(View rootView) {
        ivSticker = (ImageView) rootView.findViewById(R.id.iv_message_sticker);
        tvDate = (TextView) rootView.findViewById(R.id.txt_message_create_date);
        tvUnread = (TextView) rootView.findViewById(R.id.txt_entity_listitem_unread);

    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId) {


        Context context = ivSticker.getContext();

        ResMessages.StickerMessage stickerMessage = (ResMessages.StickerMessage) link.message;
        ResMessages.StickerContent content = stickerMessage.content;
        StickerManager.getInstance().loadStickerNoOption(ivSticker, content.groupId, content.stickerId);

        tvDate.setText(DateTransformator.getTimeStringForSimple(link.message.createTime));

        int unreadCount = UnreadCountUtil.getUnreadCount(tvUnread.getContext(), teamId, roomId,
                link.id, link.fromEntity, EntityManager.getInstance(tvUnread.getContext()).getMe().getId());

        tvUnread.setText(String.valueOf(unreadCount));
        if (unreadCount > 0) {
            tvUnread.setVisibility(View.VISIBLE);
        } else {
            tvUnread.setVisibility(View.GONE);
        }


    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_pure_sticker_v2;
    }
}
