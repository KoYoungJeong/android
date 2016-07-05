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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileCommentRefreshEvent that = (FileCommentRefreshEvent) o;

        if (teamId != that.teamId) return false;
        if (fileId != that.fileId) return false;
        if (commentId != that.commentId) return false;
        if (added != that.added) return false;
        if (eventType != null ? !eventType.equals(that.eventType) : that.eventType != null)
            return false;
        return sharedRooms != null ? sharedRooms.equals(that.sharedRooms) : that.sharedRooms == null;

    }

    @Override
    public int hashCode() {
        int result = eventType != null ? eventType.hashCode() : 0;
        result = 31 * result + (int) (teamId ^ (teamId >>> 32));
        result = 31 * result + (int) (fileId ^ (fileId >>> 32));
        result = 31 * result + (int) (commentId ^ (commentId >>> 32));
        result = 31 * result + (added ? 1 : 0);
        result = 31 * result + (sharedRooms != null ? sharedRooms.hashCode() : 0);
        return result;
    }
}
