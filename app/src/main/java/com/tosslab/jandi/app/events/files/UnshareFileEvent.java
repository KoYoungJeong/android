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
}
