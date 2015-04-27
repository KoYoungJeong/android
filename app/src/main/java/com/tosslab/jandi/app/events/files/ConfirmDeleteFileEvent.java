package com.tosslab.jandi.app.events.files;


/**
 * Created by Steve SeongUg Jung on 15. 2. 12..
 */
public class ConfirmDeleteFileEvent {
    private final int fileId;

    public ConfirmDeleteFileEvent(int fileId) {
        this.fileId = fileId;
    }

    public int getFileId() {
        return fileId;
    }
}
