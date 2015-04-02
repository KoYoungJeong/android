package com.tosslab.jandi.app.network.socket.domain;

/**
 * Created by Steve SeongUg Jung on 15. 4. 2..
 */
public class ConnectTeam {
    private final int teamId;
    private final String teamName;
    private final int memberId;
    private final String userName;

    public ConnectTeam(int teamId, String teamName, int memberId, String userName) {

        this.teamId = teamId;
        this.teamName = teamName;
        this.memberId = memberId;
        this.userName = userName;
    }

    public int getTeamId() {
        return teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public int getMemberId() {
        return memberId;
    }

    public String getUserName() {
        return userName;
    }
}
