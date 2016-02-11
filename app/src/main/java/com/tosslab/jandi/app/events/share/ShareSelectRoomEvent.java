package com.tosslab.jandi.app.events.share;

/**
 * Created by tee on 15. 9. 17..
 */
public class ShareSelectRoomEvent {

    private long roomId;
    private String roomName;
    private int roomType;

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getRoomType() {
        return roomType;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }
}
