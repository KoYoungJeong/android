package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 10. 13..
 */
public class RequestTeamCreationEvent {
    public String email;
    public RequestTeamCreationEvent(String email) {
        this.email = email;
    }
}
