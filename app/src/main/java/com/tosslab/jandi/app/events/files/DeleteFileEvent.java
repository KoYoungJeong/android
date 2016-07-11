package com.tosslab.jandi.app.events.files;

/**
 * Created by Steve SeongUg Jung on 15. 4. 7..
 */
public class DeleteFileEvent {
    private final long teamId;
    private final long id;

    public DeleteFileEvent(long teamId, long id) {
        this.teamId = teamId;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public long getTeamId() {
        return teamId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeleteFileEvent that = (DeleteFileEvent) o;

        if (teamId != that.teamId) return false;
        return id == that.id;

    }

    @Override
    public int hashCode() {
        int result = (int) (teamId ^ (teamId >>> 32));
        result = 31 * result + (int) (id ^ (id >>> 32));
        return result;
    }
}
