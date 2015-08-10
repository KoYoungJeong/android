package com.tosslab.jandi.app.network.models;

/**
 * Created by tonyjs on 15. 7. 31..
 */
public class ReqUpdatePlatformStatus {
    private String platform = "mobile";
    private boolean active = true;
    private String deviceType = "android";

    public ReqUpdatePlatformStatus(boolean active) {
        this.active = active;
    }

    public String getPlatform() {
        return platform;
    }

    public boolean isActive() {
        return active;
    }

}
