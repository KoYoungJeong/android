package com.tosslab.jandi.app.events.messages;

/**
 * Created by justinygchoi on 14. 11. 20..
 */
public class RequestDeleteMessageEvent {
    public int messageType;
    public int messageId;
    public int feedbackId;

    public RequestDeleteMessageEvent(int messageType, int messageId, int feedbackId) {
        this.messageType = messageType;
        this.messageId = messageId;
        this.feedbackId = feedbackId;
    }
}
