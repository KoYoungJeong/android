package com.tosslab.jandi.app.events.files;


/**
 * Created by Steve SeongUg Jung on 15. 2. 12..
 */
public class ConfirmDeleteFile {
    private final int fileId;

    public ConfirmDeleteFile(int fileId) {
        this.fileId = fileId;
    }

    public int getFileId() {
        return fileId;
    }
}
