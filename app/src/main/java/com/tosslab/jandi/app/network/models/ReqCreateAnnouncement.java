package com.tosslab.jandi.app.network.models;

/**
 * Created by tonyjs on 15. 6. 24..
 */

public class ReqCreateAnnouncement {
    private long messageId;

    public ReqCreateAnnouncement(long messageId) {
        this.messageId = messageId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
}
