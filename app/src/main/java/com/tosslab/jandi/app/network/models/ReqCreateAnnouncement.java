package com.tosslab.jandi.app.network.models;

/**
 * Created by tonyjs on 15. 6. 24..
 */

public class ReqCreateAnnouncement {
    private int messageId;

    public ReqCreateAnnouncement(int messageId) {
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
}
