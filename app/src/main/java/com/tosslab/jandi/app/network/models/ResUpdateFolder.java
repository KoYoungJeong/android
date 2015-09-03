package com.tosslab.jandi.app.network.models;

/**
 * Created by tee on 15. 9. 2..
 */
public class ResUpdateFolder {
    private int folderId;
    private int memberId;
    private int teamId;

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

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
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
