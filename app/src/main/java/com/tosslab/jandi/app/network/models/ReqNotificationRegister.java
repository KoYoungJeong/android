package com.tosslab.jandi.app.network.models;

/**
 * Created by justinygchoi on 2014. 7. 16..
 */
public class ReqNotificationRegister {
    public String type;
    public String token;

    public ReqNotificationRegister(String type, String token) {
        this.type = type;
        this.token = token;
    }
}
