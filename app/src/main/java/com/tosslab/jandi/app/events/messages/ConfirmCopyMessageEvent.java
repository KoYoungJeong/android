package com.tosslab.jandi.app.events.messages;

/**
 * Created by justinygchoi on 14. 11. 5..
 */
public class ConfirmCopyMessageEvent {
    public String contentString;

    public ConfirmCopyMessageEvent(String contentString) {
        this.contentString = contentString;
    }
}
