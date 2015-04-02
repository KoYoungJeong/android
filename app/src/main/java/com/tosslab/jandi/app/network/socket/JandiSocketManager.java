package com.tosslab.jandi.app.network.socket;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.socket.connector.JandiSocketConnector;
import com.tosslab.jandi.app.network.socket.connector.SocketConnector;
import com.tosslab.jandi.app.network.socket.events.EventListener;

/**
 * Created by Steve SeongUg Jung on 15. 3. 30..
 */
public class JandiSocketManager {
    private static JandiSocketManager jandiSocketManager;

    private SocketConnector socketConnector;


    private JandiSocketManager() {
        socketConnector = new JandiSocketConnector();
    }

    public static JandiSocketManager getInstance() {
        if (jandiSocketManager == null) {
            jandiSocketManager = new JandiSocketManager();
        }

        return jandiSocketManager;
    }

    synchronized public boolean connect() {
        socketConnector.connect(JandiConstantsForFlavors.SERVICE_ROOT_URL);
        return true;
    }

    synchronized public void disconnect() {
        socketConnector.disconnect();
    }

    synchronized public void register(String event, EventListener eventListener) {
        socketConnector.getEventRegister().register(event, eventListener);
    }

    synchronized public void unregister(String event, EventListener eventListener) {
        socketConnector.getEventRegister().unregister(event, eventListener);
    }

    public boolean isConnected() {
        return socketConnector.isConnected();
    }

}
