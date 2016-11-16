package com.tosslab.jandi.app.events.team;


public class TeamBadgeUpdateEvent {
    private final boolean local;

    private TeamBadgeUpdateEvent(boolean local) {this.local = local;}

    public static TeamBadgeUpdateEvent fromLocal() {return new TeamBadgeUpdateEvent(true);}

    public boolean isLocal() {
        return local;
    }
}
