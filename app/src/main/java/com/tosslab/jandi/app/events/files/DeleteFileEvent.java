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
}
