package com.tosslab.jandi.app.events.messages;

/**
 * Created by Steve SeongUg Jung on 15. 8. 5..
 */
public class MessageStarEvent {
    private final long messageId;
    private final boolean isStarred;

    public MessageStarEvent(long messageId, boolean isStarred) {

        this.messageId = messageId;
        this.isStarred = isStarred;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public long getMessageId() {
        return messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageStarEvent that = (MessageStarEvent) o;

        if (messageId != that.messageId) return false;
        return isStarred == that.isStarred;

    }

    @Override
    public int hashCode() {
        int result = (int) (messageId ^ (messageId >>> 32));
        result = 31 * result + (isStarred ? 1 : 0);
        return result;
    }
}
