package com.tosslab.toss.app.events;

/**
 * Created by justinygchoi on 2014. 5. 28..
 */
public class EditMessageEvent {
    public int cdpType;
    public int messageId;
    public EditMessageEvent(int messageId, int cdpType) {
        this.messageId = messageId;
        this.cdpType = cdpType;
    }
}
