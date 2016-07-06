package com.tosslab.jandi.app.events.messages;

/**
 * Created by Steve SeongUg Jung on 15. 4. 15..
 */
public class RoomMarkerEvent {
    private final long roomId;

    public RoomMarkerEvent(long roomId) {
        this.roomId = roomId;
    }

    public long getRoomId() {
        return roomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomMarkerEvent that = (RoomMarkerEvent) o;

        return roomId == that.roomId;

    }

    @Override
    public int hashCode() {
        return (int) (roomId ^ (roomId >>> 32));
    }
}
