package com.tosslab.jandi.app.events.files;

/**
 * Created by Steve SeongUg Jung on 15. 4. 22..
 */
public class CreateFileEvent {
    private final int teamId;
    private final int id;

    public CreateFileEvent(int teamId, int id) {
        this.teamId = teamId;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getTeamId() {
        return teamId;
    }
}
