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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkPreviewUpdateEvent that = (LinkPreviewUpdateEvent) o;

        return messageId == that.messageId;

    }

    @Override
    public int hashCode() {
        return (int) (messageId ^ (messageId >>> 32));
    }
}
