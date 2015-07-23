package com.tosslab.jandi.app.ui.message.to;

import com.tosslab.jandi.app.network.models.ResMessages;

import java.util.Date;

/**
 * Created by Steve SeongUg Jung on 15. 2. 4..
 */
public class DummyMessageLink extends ResMessages.Link {

    private final long localId;
    private String status;

    public DummyMessageLink(long localId, String message, String status) {
        this.localId = localId;

        ResMessages.TextMessage textMessage = new ResMessages.TextMessage();
        textMessage.content = new ResMessages.TextContent();
        textMessage.content.body = message;
        textMessage.createTime = new Date(System.currentTimeMillis());
        textMessage.updateTime = new Date(System.currentTimeMillis());

        this.time = new Date(System.currentTimeMillis());
        this.message = textMessage;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
