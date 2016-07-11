package com.tosslab.jandi.app.events.files;

/**
 * Created by tee on 15. 11. 25..
 */
public class UnshareFileEvent {

    private final long roomId;
    private final long fileId;

    public UnshareFileEvent(long roomId, long fileId) {
        this.roomId = roomId;
        this.fileId = fileId;
    }

    public long getRoomId() {
        return roomId;
    }

    public long getFileId() {
        return fileId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnshareFileEvent that = (UnshareFileEvent) o;

        if (roomId != that.roomId) return false;
        return fileId == that.fileId;

    }

    @Override
    public int hashCode() {
        int result = (int) (roomId ^ (roomId >>> 32));
        result = 31 * result + (int) (fileId ^ (fileId >>> 32));
        return result;
    }
}
