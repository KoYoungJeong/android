package com.tosslab.jandi.app.events.share;

/**
 * Created by tee on 15. 9. 16..
 */
public class ShareSelectTeamEvent {

    private long teamId;

    private String teamName;

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}
