package com.tosslab.jandi.app.network.socket.domain;


public class SocketUpdateMember {
    private final String token;
    private final String action;
    private final long memberId;

    private SocketUpdateMember(String token, String action, long memberId) {
        this.token = token;
        this.action = action;
        this.memberId = memberId;
    }

    public static SocketUpdateMember join(String token, long memberId) {
        return new SocketUpdateMember(token, "join", memberId);
    }

    public static SocketUpdateMember leave(String token, long memberId) {
        return new SocketUpdateMember(token, "leave", memberId);
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
