package com.tosslab.jandi.app.network.models;

/**
 * Created by justinygchoi on 2014. 7. 16..
 */
public class ReqNotificationRegister {
    public String deviceType;
    public String oldDeviceToken;
    public String newDeviceToken;

    public ReqNotificationRegister(String deviceType, String oldDeviceToken, String newDeviceToken) {
        this.deviceType = deviceType;
        this.oldDeviceToken = oldDeviceToken;
        this.newDeviceToken = newDeviceToken;
    }
}
