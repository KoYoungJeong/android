package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Date;

/**
 * Created by tonyjs on 15. 6. 24..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResAnnouncement {
    private int teamId;
    private int topicId;
    private String status;
    private int messageId;
    private int writerId;
    private String content;
    private String writtenAt;
    private int creatorId;
    private String createdAt;

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
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
                ", topicId=" + topicId +
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
