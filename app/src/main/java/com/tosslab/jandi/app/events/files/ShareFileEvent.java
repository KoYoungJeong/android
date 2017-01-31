package com.tosslab.jandi.app.events.files;

import java.util.List;

public class ShareFileEvent {
    private final long teamId;
    private final long id;
    private final List<Integer> shareEntities;

    public ShareFileEvent(long teamId, long id, List<Integer> shareEntities) {
        this.teamId = teamId;
        this.id = id;
        this.shareEntities = shareEntities;
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

        ShareFileEvent that = (ShareFileEvent) o;

        if (teamId != that.teamId) return false;
        if (id != that.id) return false;
        return shareEntities != null ? shareEntities.equals(that.shareEntities) : that.shareEntities == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (teamId ^ (teamId >>> 32));
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + (shareEntities != null ? shareEntities.hashCode() : 0);
        return result;
    }

    public List<Integer> getShareEntities() {
        return shareEntities;
    }

}
