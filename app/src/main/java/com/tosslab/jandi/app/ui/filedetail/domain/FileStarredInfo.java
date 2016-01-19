package com.tosslab.jandi.app.ui.filedetail.domain;

/**
 * Created by Steve SeongUg Jung on 15. 8. 11..
 */
public class FileStarredInfo {
    private final int fileId;
    private final boolean starred;

    public FileStarredInfo(long fileId, boolean starred) {

        this.fileId = fileId;
        this.starred = starred;
    }

    public int getFileId() {
        return fileId;
    }

    public boolean isStarred() {
        return starred;
    }
}
