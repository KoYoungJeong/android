package com.tosslab.jandi.app.network.socket.domain;

public class SocketStart {
    private final String token;
    private final String userAgent;

    public SocketStart(String token, String userAgent) {
        this.token = token;
        this.userAgent = userAgent;
    }

    public String getToken() {
        return token;
    }

    public String getUserAgent() {
        return userAgent;
    }
}
