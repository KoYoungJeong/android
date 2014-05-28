package com.tosslab.toss.app.events;

/**
 * Created by justinygchoi on 2014. 5. 28..
 */
public class DeleteMessageEvent {
    public int cdpType;
    public int messageId;

    public DeleteMessageEvent(int messageId, int cdpType) {
        this.messageId = messageId;
        this.cdpType = cdpType;
    }
}
