package com.tosslab.jandi.app.events.profile;

/**
 * Created by Steve SeongUg Jung on 15. 1. 13..
 */
public class NewEmailEvent {
    private final String email;

    public NewEmailEvent(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
