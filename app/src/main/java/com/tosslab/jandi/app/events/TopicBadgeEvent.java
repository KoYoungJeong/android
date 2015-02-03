package com.tosslab.jandi.app.events;

/**
 * Created by Steve SeongUg Jung on 15. 2. 3..
 */
public class TopicBadgeEvent {
    private final boolean isBadge;

    public TopicBadgeEvent(boolean isBadge) {
        this.isBadge = isBadge;
    }

    public boolean isBadge() {
        return isBadge;
    }
}
