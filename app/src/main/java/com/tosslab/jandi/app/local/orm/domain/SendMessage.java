package com.tosslab.jandi.app.local.orm.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.util.Collection;

/**
 * Created by Steve SeongUg Jung on 15. 7. 23..
 */
@DatabaseTable(tableName = "message_send")
public class SendMessage {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private long messageId;

    @DatabaseField
    private long roomId;
    @DatabaseField
    private String message;
    @DatabaseField
    private String status;
    @DatabaseField
    private long stickerGroupId;
    @DatabaseField
    private String stickerId;

    @ForeignCollectionField(eager = true)
    private Collection<MentionObject> mentionObjects;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
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

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public Collection<MentionObject> getMentionObjects() {
        return mentionObjects;
    }

    public void setMentionObjects(Collection<MentionObject> mentionObjects) {
        this.mentionObjects = mentionObjects;
    }

    public long getStickerGroupId() {
        return stickerGroupId;
    }

    public void setStickerGroupId(long stickerGroupId) {
        this.stickerGroupId = stickerGroupId;
    }

    public String getStickerId() {
        return stickerId;
    }

    public void setStickerId(String stickerId) {
        this.stickerId = stickerId;
    }

    public enum Status {
        SENDING, FAIL, COMPLETE
    }
}
