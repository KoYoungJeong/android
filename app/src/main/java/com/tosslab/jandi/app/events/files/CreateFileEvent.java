package com.tosslab.jandi.app.events.files;

/**
 * Created by Steve SeongUg Jung on 15. 4. 22..
 */
public class CreateFileEvent {
    private final long teamId;
    private final long id;

    public CreateFileEvent(long teamId, long id) {
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
