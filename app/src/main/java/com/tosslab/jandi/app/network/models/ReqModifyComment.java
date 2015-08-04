package com.tosslab.jandi.app.network.models;

/**
 * Created by tee on 15. 7. 28..
 */
public class ReqModifyComment {

    public String comment;

    public int teamId;


    public ReqModifyComment(String comment, int teamId) {
        this.comment = comment;
        this.teamId = teamId;
    }

    @Override
    public String toString() {
        return "ReqModifyComment{" +
                "comment='" + comment + '\'' +
                ", teamId=" + teamId +
                '}';
    }

    public String getComment() {
        return comment;
    }

    public int getTeamId() {
        return teamId;
    }
}
