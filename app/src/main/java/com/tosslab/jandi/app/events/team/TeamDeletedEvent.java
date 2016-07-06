package com.tosslab.jandi.app.events.team;

/**
 * Created by tonyjs on 16. 4. 4..
 * <p>
 * 다른 팀이 삭제된 경우에만 이벤트가 post 됨
 */
public class TeamDeletedEvent {
    private long teamId;

    public TeamDeletedEvent(long teamId) {
        this.teamId = teamId;
    }

    public long getTeamId() {
        return teamId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeamDeletedEvent that = (TeamDeletedEvent) o;

        return teamId == that.teamId;

    }

    @Override
    public int hashCode() {
        return (int) (teamId ^ (teamId >>> 32));
    }
}
