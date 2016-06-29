package com.tosslab.jandi.app.events.poll;

/**
 * Created by tonyjs on 16. 6. 28..
 */
public class RequestRefreshPollBadgeCountEvent {
    private int badgeCount;

    public RequestRefreshPollBadgeCountEvent(int badgeCount) {
        this.badgeCount = badgeCount;
    }

    public int getBadgeCount() {
        return badgeCount;
    }
}
