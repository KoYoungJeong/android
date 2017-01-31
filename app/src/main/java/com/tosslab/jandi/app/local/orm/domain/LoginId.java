package com.tosslab.jandi.app.local.orm.domain;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "loginid")
public class LoginId {
    @DatabaseField(id = true)
    private long _id = 1;
    @DatabaseField
    private String loginId;

    public LoginId() { }

    public LoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }
}
