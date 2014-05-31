package com.tosslab.toss.app.events;

/**
 * Created by justinygchoi on 2014. 5. 28..
 */
public class ConfirmDeleteMessageEvent {
    public int messageId;

    public ConfirmDeleteMessageEvent(int messageId) {
        this.messageId = messageId;
    }
}
