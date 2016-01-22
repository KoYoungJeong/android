package com.tosslab.jandi.app.events.messages;

/**
 * Created by justinygchoi on 14. 11. 20..
 */
public class RequestDeleteMessageEvent {
    public int messageType;
    public long messageId;
    public long feedbackId;

    public RequestDeleteMessageEvent(int messageType, long messageId, long feedbackId) {
        this.messageType = messageType;
        this.messageId = messageId;
        this.feedbackId = feedbackId;
    }
}
