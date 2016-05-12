package com.tosslab.jandi.app.network.models;

/**
 * Created by Steve SeongUg Jung on 14. 12. 15..
 */
public class ReqSubscribeToken {

    private boolean subscribe;

    public ReqSubscribeToken(boolean subscribe) {
        this.subscribe = subscribe;
    }

    public boolean isSubscribe() {
        return subscribe;
    }

    public void setSubscribe(boolean subscribe) {
        this.subscribe = subscribe;
    }
}
