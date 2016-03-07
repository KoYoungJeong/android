package com.tosslab.jandi.app.ui.message.v2.domain;

public class MessagePointer {
    private long firstCursorLinkId;
    private long lastReadLinkId;

    private MessagePointer(long firstCursorLinkId, long lastReadLinkId) {
        this.firstCursorLinkId = firstCursorLinkId;
        this.lastReadLinkId = lastReadLinkId;
    }

    public static MessagePointer create(long firstCursorLinkId, long lastReadLinkId) {return new MessagePointer(firstCursorLinkId, lastReadLinkId);}

    public long getFirstCursorLinkId() {
        return firstCursorLinkId;
    }

    public void setFirstCursorLinkId(long firstCursorLinkId) {
        this.firstCursorLinkId = firstCursorLinkId;
    }

    public long getLastReadLinkId() {
        return lastReadLinkId;
    }

    public void setLastReadLinkId(long lastReadLinkId) {
        this.lastReadLinkId = lastReadLinkId;
    }
}
