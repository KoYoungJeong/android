package com.tosslab.jandi.app.events.files;

/**
 * Created by Steve SeongUg Jung on 15. 4. 22..
 */
public class CreateFileEvent {
    private final int id;

    public CreateFileEvent(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
