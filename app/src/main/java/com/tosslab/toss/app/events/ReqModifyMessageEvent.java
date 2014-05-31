package com.tosslab.toss.app.events;

/**
 * Created by justinygchoi on 2014. 5. 28..
 */
public class ReqModifyMessageEvent {
    public int messageId;
    public String currentMessage;

    public ReqModifyMessageEvent(int messageId, String currentMessage) {
        this.messageId = messageId;
        this.currentMessage = currentMessage;
    }
}
