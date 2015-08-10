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
    private int teamId;
    @DatabaseField(id = true)
    @JsonProperty("topicId")
    private int roomId;
    @DatabaseField
    private String status;
    @DatabaseField
    private int messageId;
    @DatabaseField
    private int writerId;
    @DatabaseField
    private String content;
    @DatabaseField
    private String writtenAt;
    @DatabaseField
    private int creatorId;
    @DatabaseField
    private String createdAt;

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
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

    public int getWriterId() {
        return writerId;
    }

    public void setWriterId(int writerId) {
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

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
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
