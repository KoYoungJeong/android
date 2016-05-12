package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReqTargetToken {
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("device_id")
    private String deviceId;

    public ReqTargetToken(String refreshToken, String deviceId) {
        this.refreshToken = refreshToken;
        this.deviceId = deviceId;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
