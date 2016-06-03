package com.tosslab.jandi.app.events.entities;

public class MessageCreatedEvent {
    private long teamId;
    private long linkId;
    private long roomId;

    public MessageCreatedEvent(long teamId, long roomId, long linkId) {
        this.teamId = teamId;
        this.linkId = linkId;
        this.roomId = roomId;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public long getLinkId() {
        return linkId;
    }

    public void setLinkId(long linkId) {
        this.linkId = linkId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }
}
