package com.tosslab.jandi.app.events.entities;

/**
 * Created by jsuch2362 on 15. 11. 18..
 */
public class TopicThrowoutEvent {
    private final int roomId;
    private final int teamId;

    public TopicThrowoutEvent(int roomId, int teamId) {
        this.roomId = roomId;
        this.teamId = teamId;
    }

    @Override
    public String toString() {
        return "TopicThrowoutEvent{" +
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
