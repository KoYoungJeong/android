package com.tosslab.jandi.app.network.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@DatabaseTable(tableName = "announce_info")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResAnnouncement {
    @DatabaseField
    private long teamId;
    @DatabaseField(id = true)
    @JsonProperty("topicId")
    private long roomId;
    @DatabaseField
    private String status;
    @DatabaseField
    private long messageId;
    @DatabaseField
    private long writerId;
    @DatabaseField
    private String content;
    @DatabaseField
    private String writtenAt;
    @DatabaseField
    private long creatorId;
    @DatabaseField
    private String createdAt;

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
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

    public long getWriterId() {
        return writerId;
    }

    public void setWriterId(long writerId) {
        this.writerId = writerId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getWrittenAt() {
        return writtenAt;
    }

    public void setWrittenAt(String writtenAt) {
        this.writtenAt = writtenAt;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ResAnnouncement{" +
                "teamId=" + teamId +
                ", roomId=" + roomId +
                ", status='" + status + '\'' +
                ", messageId=" + messageId +
                ", writerId=" + writerId +
                ", content='" + content + '\'' +
                ", writtenAt=" + writtenAt +
                ", creatorId=" + creatorId +
                ", createdAt=" + createdAt +
                '}';
    }

    public boolean isEmpty() {
        return teamId <= 0;
    }
}
