package com.tosslab.jandi.app.network.socket.domain;

/**
 * Created by Steve SeongUg Jung on 15. 4. 2..
 */
public class ConnectTeam {
    private final String token;
    private final String userAgent;
    private final int teamId;
    private final String teamName;
    private final int memberId;
    private final String userName;

    public ConnectTeam(String token, String userAgent, long teamId, String teamName, long memberId, String userName) {
        this.token = token;
        this.userAgent = userAgent;
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

    public String getToken() {
        return token;
    }

    public String getUserAgent() {
        return userAgent;
    }
}
