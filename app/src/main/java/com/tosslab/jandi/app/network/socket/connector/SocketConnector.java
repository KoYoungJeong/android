package com.tosslab.jandi.app.network.socket.connector;

import com.tosslab.jandi.app.network.socket.events.EventListener;

import io.socket.emitter.Emitter;

/**
 * Created by Steve SeongUg Jung on 15. 4. 1..
 */
public interface SocketConnector {

    Emitter connect(String url, EventListener disconnectListener);

    void disconnect();

    boolean isConnectingOrConnected();
}
