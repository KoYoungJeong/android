package com.tosslab.jandi.app.events.team;

/**
 * Created by Steve SeongUg Jung on 15. 4. 7..
 */
public class TeamInfoChangeEvent {

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof TeamInfoChangeEvent;
    }
}
