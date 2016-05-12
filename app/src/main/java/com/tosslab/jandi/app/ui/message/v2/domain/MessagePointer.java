package com.tosslab.jandi.app.ui.message.v2.domain;

public class MessagePointer {
    private long lastReadLinkId = -1;

    private MessagePointer(long lastReadLinkId) {
        this.lastReadLinkId = lastReadLinkId;
    }

    public static MessagePointer create(long lastReadLinkId) {return new MessagePointer(lastReadLinkId);}

    public long getLastReadLinkId() {
        return lastReadLinkId;
    }

    public void setLastReadLinkId(long lastReadLinkId) {
        this.lastReadLinkId = lastReadLinkId;
    }
}
