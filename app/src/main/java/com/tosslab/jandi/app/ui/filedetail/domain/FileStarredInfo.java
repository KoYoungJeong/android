package com.tosslab.jandi.app.ui.filedetail.domain;

/**
 * Created by Steve SeongUg Jung on 15. 8. 11..
 */
public class FileStarredInfo {
    private final long fileId;
    private final boolean starred;

    public FileStarredInfo(long fileId, boolean starred) {

        this.fileId = fileId;
        this.starred = starred;
    }

    public long getFileId() {
        return fileId;
    }

    public boolean isStarred() {
        return starred;
    }
}
