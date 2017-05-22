package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Steve SeongUg Jung on 14. 12. 11..
 */
@DatabaseTable(tableName = "token")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResAccessToken {

    @DatabaseField(generatedId = true, readOnly = true)
    @JsonIgnore
    private long _id;

    @DatabaseField
    @JsonProperty("access_token")
    private String accessToken;

    @DatabaseField
    @JsonProperty("refresh_token")
    private String refreshToken;


    @DatabaseField
    @JsonProperty("token_type")
    private String tokenType;

    @DatabaseField
    @JsonProperty("expires_in")
    private String expireTime;

    @DatabaseField
    @JsonProperty("device_id")
    private String deviceId;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    public String toString() {
        return "ResAccessToken{" +
                "_id=" + _id +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expireTime='" + expireTime + '\'' +
                ", deviceId='" + deviceId + '\'' +
                '}';
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public ResAccessToken setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }
}
