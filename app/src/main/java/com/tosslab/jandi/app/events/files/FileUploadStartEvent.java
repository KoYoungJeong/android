package com.tosslab.jandi.app.events.files;

/**
 * Created by Steve SeongUg Jung on 15. 6. 17..
 */
public class FileUploadStartEvent {
    private final long entity;

    public FileUploadStartEvent(long entity) {
        this.entity = entity;
    }

    public long getEntity() {
        return entity;
    }
}
