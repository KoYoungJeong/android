package com.tosslab.jandi.app.ui.message.to;

import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.util.Date;

/**
 * Created by Steve SeongUg Jung on 15. 2. 4..
 */
public class DummyMessageLink extends ResMessages.Link {

    private final long localId;
    private SendingState sendingState;

    public DummyMessageLink(long localId, String message, SendingState sendingState) {
        this.localId = localId;

        ResMessages.TextMessage textMessage = new ResMessages.TextMessage();
        textMessage.content = new ResMessages.TextContent();
        textMessage.content.body = message;

        textMessage.writer = new ResLeftSideMenu.User();

        this.time = new Date(System.currentTimeMillis());
        this.message = textMessage;
        this.sendingState = sendingState;
    }

    public long getLocalId() {
        return localId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public SendingState getSendingState() {
        return sendingState;
    }

    public void setSendingState(SendingState sendingState) {
        this.sendingState = sendingState;
    }


}
