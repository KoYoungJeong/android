package com.tosslab.jandi.app.network.models.start;


import io.realm.RealmObject;

public class RealmLong extends RealmObject {
    private long value;

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
