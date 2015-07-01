package com.tosslab.jandi.lib.sprinkler.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Steve SeongUg Jung on 15. 7. 1..
 */
@DatabaseTable(tableName = "tracker_id")
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
}
