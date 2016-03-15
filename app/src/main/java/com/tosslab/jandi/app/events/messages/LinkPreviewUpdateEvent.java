package com.tosslab.jandi.app.events.messages;

/**
 * Created by tonyjs on 15. 9. 22..
 */
public class LinkPreviewUpdateEvent {
    private long messageId;

    public LinkPreviewUpdateEvent(long messageId) {
        this.messageId = messageId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
}
