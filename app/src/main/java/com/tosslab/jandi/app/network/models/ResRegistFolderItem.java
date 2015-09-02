package com.tosslab.jandi.app.network.models;

import java.util.Date;

/**
 * Created by tee on 15. 8. 25..
 */
public class ResRegistFolderItem {

    private int folderId;
    private int memberId;
    private int roomId;
    private int teamId;
    private Date createdAt;

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
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
