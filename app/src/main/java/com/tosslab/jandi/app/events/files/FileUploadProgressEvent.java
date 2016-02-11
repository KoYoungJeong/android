package com.tosslab.jandi.app.events.files;

/**
 * Created by Steve SeongUg Jung on 15. 6. 17..
 */
public class FileUploadProgressEvent {
    private final long entity;
    private final int progressPercent;

    public FileUploadProgressEvent(long entity, int progressPercent) {
        this.entity = entity;
        this.progressPercent = progressPercent;
    }

    public long getEntity() {
        return entity;
    }

    public int getProgressPercent() {
        return progressPercent;
    }
}
