package com.tosslab.jandi.app.events.files;

/**
 * Created by Steve SeongUg Jung on 15. 6. 17..
 */
public class FileUploadStartEvent {
    private final int entity;

    public FileUploadStartEvent(int entity) {
        this.entity = entity;
    }

    public int getEntity() {
        return entity;
    }
}
