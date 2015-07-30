package com.tosslab.jandi.app.network.models;

/**
 * Created by tonyjs on 15. 7. 30..
 */
public class ReqUpdateTopicPushSubscribe {
    private boolean subscribe;

    public ReqUpdateTopicPushSubscribe(boolean subscribe) {
        this.subscribe = subscribe;
    }

    public boolean isSubscribe() {
        return subscribe;
    }
}
