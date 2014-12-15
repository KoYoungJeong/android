package com.tosslab.jandi.app.network.models;

/**
 * Created by Steve SeongUg Jung on 14. 12. 15..
 */
public class ReqSubscibeToken {

    private String token;
    private boolean subscribe;

    public ReqSubscibeToken(String token, boolean subscribe) {
        this.subscribe = subscribe;
        this.token = token;
    }

    public boolean isSubscribe() {
        return subscribe;
    }

    public void setSubscribe(boolean subscribe) {
        this.subscribe = subscribe;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
