package com.tosslab.jandi.app.events.files;

/**
 * Created by Steve SeongUg Jung on 15. 6. 17..
 */
public class FileUploadProgressEvent {
    private final int entity;
    private final int progressPercent;

    public FileUploadProgressEvent(int entity, int progressPercent) {
        this.entity = entity;
        this.progressPercent = progressPercent;
    }

    public int getEntity() {
        return entity;
    }

    public int getProgressPercent() {
        return progressPercent;
    }
}
