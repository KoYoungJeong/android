package com.tosslab.jandi.app.events.profile;

/**
 * Created by Steve SeongUg Jung on 14. 12. 29..
 */
public class ForgotPasswordEvent {
    private final String email;

    public ForgotPasswordEvent(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
