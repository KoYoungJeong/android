package com.tosslab.jandi.app.network.models;

import android.os.Build;
import android.provider.Settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.PushTokenRepository;
import com.tosslab.jandi.app.utils.ApplicationUtil;

import java.util.List;

public class ReqAccessToken {

    @JsonProperty("grant_type")
    private final String grantType;
    @JsonProperty("model")
    private String deviceModel;
    @JsonProperty("name")
    private String deviceName;
    private String uuid;
    @JsonProperty("platform")
    private String platform;
    @JsonProperty("platform_version")
    private String platformVersion;
    @JsonProperty("app_version")
    private String appVersion;
    @JsonProperty("tokens")
    private List<PushToken> pushTokens;
    @JsonProperty("g-recaptcha-response")
    private String reCaptchaResponse;

    protected ReqAccessToken(String grantType) {
        this.grantType = grantType;
        deviceModel = Build.MODEL;
        deviceName = Build.MANUFACTURER + Build.PRODUCT;
        platform = "android";
        platformVersion = String.valueOf(Build.VERSION.SDK_INT);
        appVersion = ApplicationUtil.getAppVersionName();
        pushTokens = PushTokenRepository.getInstance().getPushTokenList();
        uuid = Settings.Secure.getString(JandiApplication.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static ReqAccessToken createPasswordReqToken(String userId, String userPassword) {
        return new ReqPasswordToken(userId, userPassword);
    }

    public static ReqAccessToken createRefreshReqToken(String refreshToken) {
        return new ReqRefreshToken(refreshToken);
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getPlatform() {
        return platform;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public List<PushToken> getPushTokens() {
        return pushTokens;
    }

    public String getGrantType() {
        return grantType;
    }

    public String getUuid() {
        return uuid;
    }

    public void setReCaptchaResponse(String reCaptchaResponse) {
        this.reCaptchaResponse = reCaptchaResponse;
    }

    static class ReqPasswordToken extends ReqAccessToken {

        @JsonProperty("username")
        private String userName;

        @JsonProperty("password")
        private String userPassword;

        ReqPasswordToken(String userName, String userPassword) {
            super("password");
            this.userName = userName;
            this.userPassword = userPassword;
        }

        public String getUserName() {
            return userName;
        }

        public String getUserPassword() {
            return userPassword;
        }
    }

    static class ReqRefreshToken extends ReqAccessToken {

        @JsonProperty("refresh_token")
        private String refreshToken;

        ReqRefreshToken(String refreshToken) {
            super("refresh_token");
            this.refreshToken = refreshToken;
        }
    }

}
