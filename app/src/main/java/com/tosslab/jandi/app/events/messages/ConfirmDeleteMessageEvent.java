package com.tosslab.jandi.app.events.messages;

/**
 * Created by justinygchoi on 2014. 5. 28..
 */
public class ConfirmDeleteMessageEvent {
    public int messageType;
    public long messageId;
    public long feedbackId;

    public ConfirmDeleteMessageEvent(int messageType, long messageId, long feedbackId) {
        this.messageType = messageType;
        this.messageId = messageId;
        this.feedbackId = feedbackId;
    }
}
