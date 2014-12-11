package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Created by justinygchoi on 2014. 9. 19..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResInvitationMembers {

    private String account;
    private String email;
    @JsonProperty("success")
    private boolean isSuccess;
    private String msg;

    public String getEmail() {
        return email;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMsg() {
        return msg;
    }

    public String getAccount() {
        return account;
    }

    @Override
    public String toString() {
        return "ResInvitationMembers{" +
                "account='" + account + '\'' +
                ", email='" + email + '\'' +
                ", isSuccess=" + isSuccess +
                ", msg='" + msg + '\'' +
                '}';
    }
}
