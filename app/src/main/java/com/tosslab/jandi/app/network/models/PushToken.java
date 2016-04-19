package com.tosslab.jandi.app.network.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "push_device_token")
public class PushToken {
    @DatabaseField(id = true)
    private String service;
    @DatabaseField
    private String token;

    public PushToken() {}

    public PushToken(String service, String token) {
        this.service = service;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String getService() {
        return service;
    }
}
