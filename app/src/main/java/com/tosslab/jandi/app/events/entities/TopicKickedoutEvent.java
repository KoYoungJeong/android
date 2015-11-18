package com.tosslab.jandi.app.events.entities;

/**
 * Created by jsuch2362 on 15. 11. 18..
 */
public class TopicKickedoutEvent {
    private final int roomId;
    private final int teamId;

    public TopicKickedoutEvent(int roomId, int teamId) {
        this.roomId = roomId;
        this.teamId = teamId;
    }

    @Override
    public String toString() {
        return "TopicKickedoutEvent{" +
                "roomId=" + roomId +
                ", teamId=" + teamId +
                '}';
    }

    public int getTeamId() {
        return teamId;
    }

    public int getRoomId() {
        return roomId;
    }
}
