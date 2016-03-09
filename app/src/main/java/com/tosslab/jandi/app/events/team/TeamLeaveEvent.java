package com.tosslab.jandi.app.events.team;

public class TeamLeaveEvent {
    private final long teamId;
    private final long memberId;

    public TeamLeaveEvent(long teamId, long memberId) {

        this.teamId = teamId;
        this.memberId = memberId;
    }

    public long getMemberId() {
        return memberId;
    }

    public long getTeamId() {
        return teamId;
    }
}
