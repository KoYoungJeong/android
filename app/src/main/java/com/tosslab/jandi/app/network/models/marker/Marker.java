package com.tosslab.jandi.app.network.models.marker;

/**
 * Created by tee on 2017. 4. 4..
 */

public class Marker {
    private long memberId;
    private long roomId;
    private long readLinkId;

    public long getMemberId() {
        return memberId;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public long getReadLinkId() {
        return readLinkId;
    }

    public void setReadLinkId(long readLinkId) {
        this.readLinkId = readLinkId;
    }
}
