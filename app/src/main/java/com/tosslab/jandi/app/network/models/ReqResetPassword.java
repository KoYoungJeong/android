package com.tosslab.jandi.app.network.models;

/**
 * Created by Steve SeongUg Jung on 14. 12. 15..
 */
public class ReqResetPassword {
    private final String token;
    private final String password;

    public ReqResetPassword(String token, String password) {
        this.token = token;
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public String getPassword() {
        return password;
    }
}
