package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Created by tonyjs on 15. 6. 2..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResAccountActivate extends ResAccessToken {
    private ResAccountInfo account;

    public ResAccountInfo getAccount() {
        return account;
    }

    public void setAccount(ResAccountInfo account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "ResAccountActivate{" +
                "account=" + account +
                '}';
    }
}
