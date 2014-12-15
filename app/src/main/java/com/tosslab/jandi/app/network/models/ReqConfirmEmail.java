package com.tosslab.jandi.app.network.models;

/**
 * Created by Steve SeongUg Jung on 14. 12. 15..
 */
public class ReqConfirmEmail {
    private final String email;
    private final String token;

    public ReqConfirmEmail(String email, String token) {
        this.email = email;
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }
}
