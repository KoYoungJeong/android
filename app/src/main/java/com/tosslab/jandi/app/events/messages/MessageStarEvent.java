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
}
