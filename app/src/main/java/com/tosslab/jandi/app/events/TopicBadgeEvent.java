package com.tosslab.jandi.app.events;

/**
 * Created by Steve SeongUg Jung on 15. 2. 3..
 */
public class TopicBadgeEvent {
    private final boolean isBadge;
    private final int count;

    public TopicBadgeEvent(boolean isBadge, int count) {
        this.isBadge = isBadge;
        this.count = count;
    }

    public boolean isBadge() {
        return isBadge;
    }

    public int getCount() {
        return count;
    }
}
