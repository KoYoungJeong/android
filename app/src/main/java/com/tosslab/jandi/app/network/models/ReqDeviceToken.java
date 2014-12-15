package com.tosslab.jandi.app.network.models;

/**
 * Created by Steve SeongUg Jung on 14. 12. 15..
 */
public class ReqDeviceToken {
    private String token;

    public ReqDeviceToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
