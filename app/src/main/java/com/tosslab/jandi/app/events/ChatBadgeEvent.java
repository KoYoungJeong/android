package com.tosslab.jandi.app.events;

/**
 * Created by Steve SeongUg Jung on 15. 2. 3..
 */
public class ChatBadgeEvent {

    private final boolean isBadge;

    public ChatBadgeEvent(boolean isBadge) {
        this.isBadge = isBadge;
    }

    public boolean isBadge() {
        return isBadge;
    }
}
