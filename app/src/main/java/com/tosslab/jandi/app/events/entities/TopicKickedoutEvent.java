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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TopicKickedoutEvent that = (TopicKickedoutEvent) o;

        if (roomId != that.roomId) return false;
        return teamId == that.teamId;

    }

    @Override
    public int hashCode() {
        int result = roomId;
        result = 31 * result + teamId;
        return result;
    }
}
