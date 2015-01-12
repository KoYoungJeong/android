package com.tosslab.jandi.app.network.models;

/**
 * Created by Steve SeongUg Jung on 15. 1. 13..
 */
public class ReqUpdatePrimaryEmailInfo {

    private final String primaryEmail;

    public ReqUpdatePrimaryEmailInfo(String primaryEmail) {
        this.primaryEmail = primaryEmail;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }
}
