package com.tosslab.jandi.app.events.files;


/**
 * Created by Steve SeongUg Jung on 15. 2. 12..
 */
public class ConfirmDeleteFileEvent {
    private final long fileId;

    public ConfirmDeleteFileEvent(long fileId) {
        this.fileId = fileId;
    }

    public long getFileId() {
        return fileId;
    }
}
