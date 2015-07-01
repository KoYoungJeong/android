package com.tosslab.jandi.lib.sprinkler.domain;

/**
 * Created by Steve SeongUg Jung on 15. 7. 1..
 */
public class TrackerId {
    private String token;
    private int accountId;
    private int memberId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }
}
