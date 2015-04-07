package com.tosslab.jandi.app.network.socket.connector;

import com.github.nkzawa.emitter.Emitter;
import com.tosslab.jandi.app.network.socket.events.EventListener;

/**
 * Created by Steve SeongUg Jung on 15. 4. 1..
 */
public interface SocketConnector {

    Emitter connect(String url, EventListener disconnectEventListener);

    void disconnect();

    boolean isConnectingOrConnected();
}
