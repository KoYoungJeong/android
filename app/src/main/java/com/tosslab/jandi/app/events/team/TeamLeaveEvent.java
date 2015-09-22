package com.tosslab.jandi.app.events.team;

public class TeamLeaveEvent {
    private final int teamId;
    private final int memberId;

    public TeamLeaveEvent(int teamId, int memberId) {

        this.teamId = teamId;
        this.memberId = memberId;
    }

    public int getMemberId() {
        return memberId;
    }

    public int getTeamId() {
        return teamId;
    }
}
