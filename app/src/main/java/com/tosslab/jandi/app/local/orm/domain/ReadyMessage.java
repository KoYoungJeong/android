package com.tosslab.jandi.app.local.orm.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Steve SeongUg Jung on 15. 7. 23..
 */
@DatabaseTable(tableName = "message_ready")
public class ReadyMessage {

    @DatabaseField(id = true)
    private long roomId;
    @DatabaseField
    private String text;

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
