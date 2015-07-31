package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.utils.IonCircleTransform;

/**
 * Created by Steve SeongUg Jung on 15. 2. 4..
 */
public class DummyViewHolder implements BodyViewHolder {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView messageTextView;
    private ImageView ivStatus;

    @Override
    public void initView(View rootView) {
        profileImageView = (ImageView) rootView.findViewById(R.id.img_message_user_profile);
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_user_name);
        messageTextView = (TextView) rootView.findViewById(R.id.txt_message_content);
        ivStatus = ((ImageView) rootView.findViewById(R.id.iv_message_sending_status));
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {

        DummyMessageLink dummyMessageLink = (DummyMessageLink) link;

        FormattedEntity entity = EntityManager.getInstance(nameTextView.getContext())
                .getEntityById(dummyMessageLink.message.writerId);

        String profileUrl = entity.getUserLargeProfileUrl();

        Ion.with(profileImageView)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new IonCircleTransform())
                .crossfade(true)
                .load(profileUrl);

        nameTextView.setText(entity.getName());

        if (link.message instanceof ResMessages.TextMessage) {
            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
            messageTextView.setText(textMessage.content.body);
        }
        SendMessage.Status status = SendMessage.Status.valueOf(dummyMessageLink.getStatus());
        int textColor = nameTextView.getContext().getResources().getColor(R.color.jandi_messages_name);
        switch (status) {
            case FAIL:
                ivStatus.setVisibility(View.VISIBLE);
                ivStatus.setImageResource(R.drawable.jandi_icon_message_failure);
                profileImageView.setAlpha(0.3f);
                nameTextView.setTextColor(textColor & 0x30FFFFFF);
                messageTextView.setTextColor(textColor & 0x30FFFFFF);
                break;
            case SENDING:
                ivStatus.setVisibility(View.VISIBLE);
                ivStatus.setImageResource(R.drawable.jandi_icon_message_sending);
                profileImageView.setAlpha(1f);
                nameTextView.setTextColor(textColor);
                messageTextView.setTextColor(textColor);
                break;
            case COMPLETE:
                ivStatus.setVisibility(View.INVISIBLE);
                profileImageView.setAlpha(1f);
                nameTextView.setTextColor(textColor);
                messageTextView.setTextColor(textColor);
                break;
        }

    }

    @Override
    public void setLastReadViewVisible(int currentLinkId, int lastReadLinkId) {

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_dummy_v2;

    }

}
