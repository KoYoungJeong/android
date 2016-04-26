package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Steve SeongUg Jung on 14. 12. 11..
 */
public class ReqAccessToken {

    @JsonProperty("grant_type")
    private String grantType;

    protected ReqAccessToken(String grantType) {
        this.grantType = grantType;
    }

    public static ReqAccessToken createPasswordReqToken(String userId, String userPassword) {
        return new ReqPasswordToken(userId, userPassword);
    }

    public static ReqAccessToken createRefreshReqToken(String refreshToken) {
        return new ReqRefreshToken(refreshToken);
    }

    public String getGrantType() {
        return grantType;
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
