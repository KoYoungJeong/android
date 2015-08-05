package com.tosslab.jandi.app.events.messages;

/**
 * Created by Steve SeongUg Jung on 15. 8. 5..
 */
public class SocketMessageStarEvent {
    private final int messageId;
    private final boolean isStarred;

    public SocketMessageStarEvent(int messageId, boolean isStarred) {

        this.messageId = messageId;
        this.isStarred = isStarred;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public int getMessageId() {
        return messageId;
    }
}
