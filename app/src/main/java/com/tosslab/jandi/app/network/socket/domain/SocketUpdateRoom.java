package com.tosslab.jandi.app.network.socket.domain;


public class SocketUpdateRoom {
    private final String token;
    private final String action;
    private final long memberId;
    private final long roomId;

    private SocketUpdateRoom(String token, String action, long memberId, long roomId) {
        this.token = token;
        this.action = action;
        this.memberId = memberId;
        this.roomId = roomId;
    }

    public static SocketUpdateRoom join(String token, long memberId, long roomId) {
        return new SocketUpdateRoom(token, "join", memberId, roomId);
    }

    public static SocketUpdateRoom leave(String token, long memberId, long roomId) {
        return new SocketUpdateRoom(token, "leave", memberId, roomId);
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

    public long getRoomId() {
        return roomId;
    }
}
