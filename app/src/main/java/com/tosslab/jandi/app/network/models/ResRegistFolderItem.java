package com.tosslab.jandi.app.network.models;

import java.util.Date;

/**
 * Created by tee on 15. 8. 25..
 */
public class ResRegistFolderItem {

    private long folderId;
    private long memberId;
    private long roomId;
    private long teamId;
    private Date createdAt;

    public long getFolderId() {
        return folderId;
    }

    public void setFolderId(long folderId) {
        this.folderId = folderId;
    }

    public long getMemberId() {
        return memberId;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ResRegistFolderItem{" +
                "folderId=" + folderId +
                ", memberId=" + memberId +
                ", roomId=" + roomId +
                ", teamId=" + teamId +
                ", createdAt=" + createdAt +
                '}';
    }
}
