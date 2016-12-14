package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Created by justinygchoi on 2014. 9. 19..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResInvitationMembers {

    private String email;
    @JsonProperty("success")
    private boolean isSuccess;
    private int code;
    private String msg;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "ResInvitationMembers{" +
                ", email='" + email + '\'' +
                ", isSuccess=" + isSuccess +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
