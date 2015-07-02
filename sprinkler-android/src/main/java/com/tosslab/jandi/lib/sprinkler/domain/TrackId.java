package com.tosslab.jandi.lib.sprinkler.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Steve SeongUg Jung on 15. 7. 1..
 */
@DatabaseTable(tableName = "trackId")
public class TrackId {
    @DatabaseField(generatedId = true, readOnly = true)
    private long id;

    @DatabaseField
    private String token;
    @DatabaseField
    private int accountId;
    @DatabaseField
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public static class Builder {
        private String token;
        private int accountId;
        private int memberId;

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder accountId(int accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder memberId(int memberId) {
            this.memberId = memberId;
            return this;
        }

        public TrackId build() {
            TrackId trackId = new TrackId();
            trackId.setToken(token);
            trackId.setMemberId(memberId);
            trackId.setAccountId(accountId);
            return trackId;
        }
    }
}
