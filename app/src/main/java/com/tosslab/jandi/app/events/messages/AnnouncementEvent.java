package com.tosslab.jandi.app.events.messages;

/**
 * Created by tonyjs on 15. 6. 24..
 */
public class AnnouncementEvent {
    public enum Action {
        CREATE, DELETE
    }

    private int messageId;
    private Action action;

    public AnnouncementEvent(Action action) {
        this.action = action;
    }

    public AnnouncementEvent(Action action, int messageId) {
        this.action = action;
        this.messageId = messageId;
    }

    public Action getAction() {
        return action;
    }

    public int getMessageId() {
        return messageId;
    }
}
