package com.tosslab.jandi.app.events.files;

/**
 * Created by Steve SeongUg Jung on 15. 4. 7..
 */
public class DeleteFileEvent {
    private final int id;

    public DeleteFileEvent(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}