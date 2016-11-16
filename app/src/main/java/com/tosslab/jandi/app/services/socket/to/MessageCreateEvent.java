package com.tosslab.jandi.app.services.socket.to;

public class MessageCreateEvent {
    private final long teamId;

    public MessageCreateEvent(long teamId) {this.teamId = teamId;}

    public long getTeamId() {
        return teamId;
    }
}
