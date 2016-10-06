package com.tosslab.jandi.app.network.models;

/**
 * Created by tonyjs on 2016. 9. 20..
 */
public class ReqMentionMarkerUpdate {
    private long messageId;

    private ReqMentionMarkerUpdate(long messageId) {
        this.messageId = messageId;
    }

    public static ReqMentionMarkerUpdate create(long messageId) {
        return new ReqMentionMarkerUpdate(messageId);
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
}
