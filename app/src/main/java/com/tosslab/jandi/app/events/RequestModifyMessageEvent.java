package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 5. 28..
 */
public class RequestModifyMessageEvent {
    public int messageId;
    public String currentMessage;
    public int feedbackId;
    public int messageType;

    public RequestModifyMessageEvent(int messageType, int messageId, String currentMessage, int feedbackId) {
        this.messageType = messageType;
        this.messageId = messageId;
        this.currentMessage = currentMessage;
        this.feedbackId = feedbackId;
    }
}
