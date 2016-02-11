package com.tosslab.jandi.app.events.files;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 4. 7..
 */
public class FileCommentRefreshEvent {
    private final String eventType;
    private final int fileId;
    private final int commentId;
    private final boolean added;
    private List<Long> sharedRooms;
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

    public List<Long> getSharedRooms() {
        return sharedRooms;
    }

    public void setSharedRooms(List<Long> sharedRooms) {
        this.sharedRooms = sharedRooms;
    }
}
