package com.tosslab.jandi.app.events.team;

/**
 * Created by Steve SeongUg Jung on 15. 4. 7..
 */
public class TeamJoinEvent {

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof TeamJoinEvent;
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
