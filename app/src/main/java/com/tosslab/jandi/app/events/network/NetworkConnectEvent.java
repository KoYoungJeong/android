package com.tosslab.jandi.app.events.network;

/**
 * Created by Steve SeongUg Jung on 15. 8. 8..
 */
public class NetworkConnectEvent {
    private final boolean connected;

    public NetworkConnectEvent(boolean connected) {

        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }
}
