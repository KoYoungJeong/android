package com.tosslab.jandi.app.events.files;

public class ShareFileEvent {
    private final long teamId;
    private final long id;

    public ShareFileEvent(long teamId, long id) {
        this.teamId = teamId;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public long getTeamId() {
        return teamId;
    }
}
