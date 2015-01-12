package com.tosslab.jandi.app.events.profile;

/**
 * Created by Steve SeongUg Jung on 15. 1. 13..
 */
public class RetryNewEmailEvent {
    private final String email;

    public RetryNewEmailEvent(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
