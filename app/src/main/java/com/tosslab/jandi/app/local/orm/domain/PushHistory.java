package com.tosslab.jandi.app.local.orm.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "push_history")
public class PushHistory {
    @DatabaseField(id = true)
    private long messageId;

    public PushHistory(long messageId) {this.messageId = messageId;}

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
}
