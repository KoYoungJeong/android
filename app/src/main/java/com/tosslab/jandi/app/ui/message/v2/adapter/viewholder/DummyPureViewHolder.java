package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;

/**
 * Created by Steve SeongUg Jung on 15. 2. 4..
 */
public class DummyPureViewHolder implements BodyViewHolder {

    private TextView messageTextView;
    private ImageView ivStatus;

    @Override
    public void initView(View rootView) {
        messageTextView = (TextView) rootView.findViewById(R.id.txt_message_content);
        ivStatus = ((ImageView) rootView.findViewById(R.id.iv_message_sending_status));
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {

        DummyMessageLink dummyMessageLink = (DummyMessageLink) link;

        if (link.message instanceof ResMessages.TextMessage) {
            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
            messageTextView.setText(textMessage.content.body);
        }

        SendMessage.Status status = SendMessage.Status.valueOf(dummyMessageLink.getStatus());
        int textColor = messageTextView.getContext().getResources().getColor(R.color.jandi_messages_name);
        switch (status) {
            case FAIL:
                ivStatus.setVisibility(View.VISIBLE);
                ivStatus.setImageResource(R.drawable.jandi_icon_message_failure);
                messageTextView.setTextColor(textColor & 0x30FFFFFF);
                break;
            case SENDING:
                ivStatus.setVisibility(View.VISIBLE);
                ivStatus.setImageResource(R.drawable.jandi_icon_message_sending);
                messageTextView.setTextColor(textColor);
                break;
            case COMPLETE:
                ivStatus.setVisibility(View.INVISIBLE);
                messageTextView.setTextColor(textColor);
                break;
        }

    }

    @Override
    public void setLastReadViewVisible(int currentLinkId, int lastReadLinkId) {
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_dummy_pure_v2;

    }

}
