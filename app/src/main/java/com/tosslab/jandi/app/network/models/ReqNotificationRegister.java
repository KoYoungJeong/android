package com.tosslab.jandi.app.network.models;

/**
 * Created by justinygchoi on 2014. 7. 16..
 */
public class ReqNotificationRegister {
    public String deviceType;
    public String deviceToken;
    public ReqNotificationRegister(String deviceType, String deviceToken) {
        this.deviceType = deviceType;
        this.deviceToken = deviceToken;
    }
}
