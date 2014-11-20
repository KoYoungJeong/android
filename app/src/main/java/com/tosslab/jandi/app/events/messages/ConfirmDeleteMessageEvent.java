package com.tosslab.jandi.app.events.messages;

/**
 * Created by justinygchoi on 2014. 5. 28..
 */
public class ConfirmDeleteMessageEvent {
    public int messageType;
    public int messageId;
    public int feedbackId;

    public ConfirmDeleteMessageEvent(int messageType, int messageId, int feedbackId) {
        this.messageType = messageType;
        this.messageId = messageId;
        this.feedbackId = feedbackId;
    }
}
