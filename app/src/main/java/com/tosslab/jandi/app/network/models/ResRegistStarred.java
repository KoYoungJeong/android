package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;

/**
 * Created by tee on 15. 7. 27..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResRegistStarred {
    private long messageId;
    private long roomId;
    private long teamId;
    private long memberId;
    private Date createTime;

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

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public long getMemberId() {
        return memberId;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ResRegistStarred{" +
                "messageId" + messageId +
                "teamId=" + teamId +
                ", roomId=" + roomId +
                ", memberId='" + memberId +
                ", createTime=" + createTime +
                '}';
    }
}
