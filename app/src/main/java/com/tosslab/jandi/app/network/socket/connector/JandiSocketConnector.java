package com.tosslab.jandi.app.network.socket.connector;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.tosslab.jandi.app.network.socket.events.register.EventRegister;
import com.tosslab.jandi.app.network.socket.events.register.JandiEventRegister;

import java.net.URISyntaxException;

/**
 * Created by Steve SeongUg Jung on 15. 4. 1..
 */
public class JandiSocketConnector implements SocketConnector {

    private Socket socket;
    private EventRegister eventRegister;

    public JandiSocketConnector() {
        eventRegister = new JandiEventRegister(null);
    }

    @Override
    public void connect(String url) {
        if (socket != null && socket.connected()) {
            return;
        }

        if (socket == null) {
            try {
                IO.Options options = new IO.Options();
                options.reconnection = true;

                socket = IO.socket(url, options);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        if (socket != null) {
            socket.connect();
        }

    }

    @Override
    public void disconnect() {
        if (socket != null && socket.connected()) {
            socket.disconnect();
        }
    }

    @Override
    public boolean isConnected() {
        return socket.connected();
    }

    @Override
    public EventRegister getEventRegister() {
        return eventRegister;
    }
}
