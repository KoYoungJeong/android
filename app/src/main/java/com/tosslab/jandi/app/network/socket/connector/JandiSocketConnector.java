package com.tosslab.jandi.app.network.socket.connector;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.tosslab.jandi.app.network.socket.events.EventListener;

import java.net.URISyntaxException;

/**
 * Created by Steve SeongUg Jung on 15. 4. 1..
 */
public class JandiSocketConnector implements SocketConnector {

    private Socket socket;
    private boolean connectingOrConnected;

    public JandiSocketConnector() {
        connectingOrConnected = false;
    }

    @Override
    public Emitter connect(String url, EventListener disconnectListener) {
        if (socket != null && socket.connected()) {
            connectingOrConnected = true;
            return socket;
        }

        if (socket == null) {
            try {
                IO.Options options = new IO.Options();
                options.reconnection = false;
                options.multiplex = false;
                options.forceNew = false;

                socket = IO.socket(url, options);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        if (socket != null) {
            connectingOrConnected = true;
            socket
                    .on(Socket.EVENT_DISCONNECT, args -> disconnectCallback(disconnectListener, args))
                    .on(Socket.EVENT_CONNECT_ERROR, args -> disconnectCallback(disconnectListener, args))
                    .on(Socket.EVENT_CONNECT_TIMEOUT, args -> disconnectCallback(disconnectListener, args));
            socket.connect();
        }

        return socket;
    }

    private void disconnectCallback(EventListener disconnectListener, Object[] args) {
        connectingOrConnected = false;
        if (disconnectListener != null) {
            disconnectListener.callback(args);
        }
    }

    @Override
    public void disconnect() {
        if (socket != null && socket.connected()) {
            connectingOrConnected = false;
            socket.disconnect();
        }
    }

    @Override
    public boolean isConnectingOrConnected() {
        return connectingOrConnected;
    }

}
