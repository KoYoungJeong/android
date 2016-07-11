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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeamLeaveEvent that = (TeamLeaveEvent) o;

        if (teamId != that.teamId) return false;
        return memberId == that.memberId;

    }

    @Override
    public int hashCode() {
        int result = (int) (teamId ^ (teamId >>> 32));
        result = 31 * result + (int) (memberId ^ (memberId >>> 32));
        return result;
    }
}
