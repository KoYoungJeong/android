package com.tosslab.jandi.app.events.messages;

/**
 * Created by tee on 15. 8. 3..
 */
public class RefreshOldStarMentionedEvent {

    String type;

    public RefreshOldStarMentionedEvent(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
