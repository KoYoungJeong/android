package com.tosslab.jandi.app.events.messages;

/**
 * Created by tee on 15. 7. 30..
 */
public class MessageStarredEvent {

    private long messageId;
    private Action action;

    public MessageStarredEvent(Action action) {
        this.action = action;
    }

    public MessageStarredEvent(Action action, long messageId) {
        this.action = action;
        this.messageId = messageId;
    }

    public Action getAction() {
        return action;
    }

    public long getMessageId() {
        return messageId;
    }

    public enum Action {
        STARRED, UNSTARRED, DUMMY
    }

}
