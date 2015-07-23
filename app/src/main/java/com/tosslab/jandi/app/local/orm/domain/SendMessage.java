package com.tosslab.jandi.app.local.orm.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Steve SeongUg Jung on 15. 7. 23..
 */
@DatabaseTable(tableName = "message_send")
public class SendMessage {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private int messageId;

    @DatabaseField
    private int roomId;
    @DatabaseField
    private String message;
    @DatabaseField
    private String status;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public enum Status {
        SENDING, FIAL, COMPLETE
    }
}
