package com.tosslab.jandi.app.events.files;

/**
 * Created by Steve SeongUg Jung on 15. 4. 7..
 */
public class FileCommentRefreshEvent {
    private final String eventType;
    private final int fileId;
    private final int commentId;
    private final boolean added;

    public FileCommentRefreshEvent(String eventType, int fileId, int commentId, boolean added) {
        this.eventType = eventType;
        this.fileId = fileId;
        this.commentId = commentId;
        this.added = added;
    }

    public int getFileId() {
        return fileId;
    }

    public String getEventType() {
        return eventType;
    }

    public int getCommentId() {
        return commentId;
    }

    public boolean isAdded() {
        return added;
    }
}
