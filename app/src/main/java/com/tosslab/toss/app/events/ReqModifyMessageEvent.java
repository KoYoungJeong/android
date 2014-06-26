package com.tosslab.toss.app.events;

/**
 * Created by justinygchoi on 2014. 5. 28..
 */
public class ReqModifyMessageEvent {
    public int messageId;
    public String currentMessage;
    public int feedbackId;
    public int messageType;

    public ReqModifyMessageEvent(int messageType, int messageId, String currentMessage, int feedbackId) {
        this.messageType = messageType;
        this.messageId = messageId;
        this.currentMessage = currentMessage;
        this.feedbackId = feedbackId;
    }
}
