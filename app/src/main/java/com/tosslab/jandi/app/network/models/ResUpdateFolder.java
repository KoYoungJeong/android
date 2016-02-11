package com.tosslab.jandi.app.network.models;

/**
 * Created by tee on 15. 9. 2..
 */
public class ResUpdateFolder {
    private long folderId;
    private long memberId;
    private long teamId;

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

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    @Override
    public String toString() {
        return "ResUpdateFolder{" +
                "folderId=" + folderId +
                ", memberId=" + memberId +
                ", teamId=" + teamId +
                '}';
    }
}
