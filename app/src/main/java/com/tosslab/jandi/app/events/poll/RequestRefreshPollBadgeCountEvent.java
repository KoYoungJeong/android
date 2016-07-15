package com.tosslab.jandi.app.events.poll;

/**
 * Created by tonyjs on 16. 7. 8..
 */
public class RequestRefreshPollBadgeCountEvent {

    private long teamId;

    public RequestRefreshPollBadgeCountEvent(long teamId) {
        this.teamId = teamId;
    }

    public long getTeamId() {
        return teamId;
    }
}
