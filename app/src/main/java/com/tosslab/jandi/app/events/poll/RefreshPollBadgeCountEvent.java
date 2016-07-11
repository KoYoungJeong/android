package com.tosslab.jandi.app.events.poll;

/**
 * Created by tonyjs on 16. 6. 28..
 */
public class RefreshPollBadgeCountEvent {
    private int badgeCount;

    public RefreshPollBadgeCountEvent(int badgeCount) {
        this.badgeCount = badgeCount;
    }

    public int getBadgeCount() {
        return badgeCount;
    }
}
