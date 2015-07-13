package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.sticker.StickerManager;
import com.tosslab.jandi.app.utils.DateTransformator;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 6. 9..
 */
public class PureStickerCommentViewHolder implements BodyViewHolder {

    private TextView nameTextView;
    private TextView dateTextView;
    private ImageView ivSticker;
    private View disableLineThroughView;
    private TextView unreadTextView;

    @Override
    public void initView(View rootView) {
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_nested_comment_user_name);
        dateTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_create_date);
        ivSticker = (ImageView) rootView.findViewById(R.id.iv_message_nested_comment_content);
        disableLineThroughView = rootView.findViewById(R.id.img_entity_listitem_line_through);
        unreadTextView = (TextView) rootView.findViewById(R.id.txt_entity_listitem_unread);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId) {
        int fromEntityId = link.fromEntity;

        ResMessages.CommentStickerMessage stickerMessage = (ResMessages.CommentStickerMessage) link.message;

        FormattedEntity entity = EntityManager.getInstance(nameTextView.getContext()).getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        EntityManager entityManager = EntityManager.getInstance(nameTextView.getContext());
        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
        ResLeftSideMenu.User user = entityById.getUser();
        if (entityById != null && user != null && TextUtils.equals(user.status, "enabled")) {
            disableLineThroughView.setVisibility(View.GONE);
            nameTextView.setTextColor(nameTextView.getResources().getColor(R.color.jandi_messages_name));
        } else {
            nameTextView.setTextColor(nameTextView.getResources().getColor(R.color.deactivate_text_color));
            disableLineThroughView.setVisibility(View.VISIBLE);
        }

        nameTextView.setText(fromEntity.name);
        dateTextView.setText(DateTransformator.getTimeStringForSimple(link.time));

        int unreadCount = UnreadCountUtil.getUnreadCount(unreadTextView.getContext(),
                teamId, roomId, link.id, fromEntityId, entityManager.getMe().getId());

        unreadTextView.setText(String.valueOf(unreadCount));
        if (unreadCount <= 0) {
            unreadTextView.setVisibility(View.GONE);
        } else {
            unreadTextView.setVisibility(View.VISIBLE);
        }

        StickerManager.getInstance().loadStickerNoOption(ivSticker, stickerMessage.content.groupId, stickerMessage.content.stickerId);

        nameTextView.setOnClickListener(v ->
                EventBus.getDefault().post(new RequestUserInfoEvent(fromEntity.id)));

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_sticker_cmt_without_file_v2;
    }
}
