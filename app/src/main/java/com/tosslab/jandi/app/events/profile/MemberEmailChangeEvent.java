package com.tosslab.jandi.app.events.profile;

/**
 * Created by Steve SeongUg Jung on 15. 1. 15..
 */
public class MemberEmailChangeEvent {
    private final String email;

    public MemberEmailChangeEvent(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
