package com.tosslab.jandi.app.events;

/**
 * Created by tonyjs on 2016. 8. 19..
 */
public class NavigationBadgeEvent {

    private int badgeCount;

    public NavigationBadgeEvent(int badgeCount) {
        this.badgeCount = badgeCount;
    }

    public int getBadgeCount() {
        return badgeCount;
    }
}
