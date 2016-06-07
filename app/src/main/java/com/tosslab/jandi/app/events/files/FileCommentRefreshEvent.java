package com.tosslab.jandi.app.events.files;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 4. 7..
 */
public class FileCommentRefreshEvent {
    private final String eventType;
    private final long teamId;
    private final long fileId;
    private final long commentId;
    private final boolean added;
    private List<Long> sharedRooms;

    public FileCommentRefreshEvent(String eventType, long teamId,
                                   long fileId, long commentId, boolean added) {
        this.eventType = eventType;
        this.teamId = teamId;
        this.fileId = fileId;
        this.commentId = commentId;
        this.added = added;
    }

    public long getTeamId() {
        return teamId;
    }

    public long getFileId() {
        return fileId;
    }

    public String getEventType() {
        return eventType;
    }

    public long getCommentId() {
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
