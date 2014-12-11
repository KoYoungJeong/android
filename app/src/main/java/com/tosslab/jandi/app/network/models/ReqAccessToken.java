package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by Steve SeongUg Jung on 14. 12. 11..
 */
public class ReqAccessToken {

    @JsonProperty("grant_type")
    private String grantType;

    @JsonProperty("username")
    private String userId;

    @JsonProperty("password")
    private String userPassword;

    private ReqAccessToken(String grantType, String userId, String userPassword) {
        this.grantType = grantType;
        this.userId = userId;
        this.userPassword = userPassword;
    }

    public static ReqAccessToken createPasswordReqToken(String userId, String userPassword) {
        return new ReqAccessToken("password", userId, userPassword);
    }

    public static ReqAccessToken createRefreshReqToken(String userId, String userPassword) {
        return new ReqAccessToken("refresh_token", userId, userPassword);
    }
}
