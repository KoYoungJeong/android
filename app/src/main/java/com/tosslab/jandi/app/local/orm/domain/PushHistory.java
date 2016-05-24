package com.tosslab.jandi.app.local.orm.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "push_history")
public class PushHistory {
    @DatabaseField(id = true)
    private long messageId;

    @DatabaseField
    private long roomId;

    public PushHistory() {
    }

    public PushHistory(long roomId, long messageId) {
        this.roomId = roomId;
        this.messageId = messageId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }
}
