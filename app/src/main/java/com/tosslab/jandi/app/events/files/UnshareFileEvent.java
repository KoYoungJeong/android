package com.tosslab.jandi.app.events.files;

/**
 * Created by tee on 15. 11. 25..
 */
public class UnshareFileEvent {

    private final int roomId;
    private final int fileId;

    public UnshareFileEvent(int roomId, int fileId) {
        this.roomId = roomId;
        this.fileId = fileId;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getFileId() {
        return fileId;
    }
}
