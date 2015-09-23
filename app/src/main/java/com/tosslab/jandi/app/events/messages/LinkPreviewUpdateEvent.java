package com.tosslab.jandi.app.events.messages;

/**
 * Created by tonyjs on 15. 9. 22..
 */
public class LinkPreviewUpdateEvent {
    private int messageId;

    public LinkPreviewUpdateEvent(int messageId) {
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
}
