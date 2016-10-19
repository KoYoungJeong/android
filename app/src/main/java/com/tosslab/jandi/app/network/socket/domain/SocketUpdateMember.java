package com.tosslab.jandi.app.network.socket.domain;


public class SocketUpdateMember {
    private final String token;
    private final String action;
    private final long memberId;
    private final long teamId;

    private SocketUpdateMember(String token, String action, long memberId, long teamId) {
        this.token = token;
        this.action = action;
        this.memberId = memberId;
        this.teamId = teamId;
    }

    public static SocketUpdateMember join(String token, long memberId, long teamId) {
        return new SocketUpdateMember(token, "join", memberId, teamId);
    }

    public static SocketUpdateMember leave(String token, long memberId, long teamId) {
        return new SocketUpdateMember(token, "leave", memberId, teamId);
    }

    public String getToken() {
        return token;
    }

    public String getAction() {
        return action;
    }

    public long getMemberId() {
        return memberId;
    }
}
