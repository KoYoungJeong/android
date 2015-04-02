package com.tosslab.jandi.app.network.socket.connector;

import com.tosslab.jandi.app.network.socket.events.register.EventRegister;

/**
 * Created by Steve SeongUg Jung on 15. 4. 1..
 */
public interface SocketConnector {

    void connect(String url);

    void disconnect();

    boolean isConnected();

    EventRegister getEventRegister();
}
