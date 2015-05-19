package com.tosslab.jandi.app.network.socket.connector;

import android.util.Log;

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
                    .on(Socket.EVENT_DISCONNECT, args -> disconnectCallbackWithLog(disconnectListener, args, Socket.EVENT_DISCONNECT))
                    .on(Socket.EVENT_ERROR, args -> disconnectCallbackWithLog(disconnectListener, args, Socket.EVENT_ERROR))
                    .on(Socket.EVENT_CONNECT_ERROR, args -> disconnectCallbackWithLog(disconnectListener, args, Socket.EVENT_CONNECT_ERROR))
                    .on(Socket.EVENT_CONNECT_TIMEOUT, args -> disconnectCallbackWithLog(disconnectListener, args, Socket.EVENT_CONNECT_TIMEOUT));
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


    private void disconnectCallbackWithLog(EventListener disconnectListener, Object[] args, String event) {

        Log.d("INFO", "Disconnect : " + event);
        if (args != null) {
            for (Object arg : args) {
                Log.d("INFO", "Disconnect Reason : " + arg.toString());
            }
        }

        disconnectCallback(disconnectListener, args);
    }

    @Override
    public void disconnect() {

        if (connectingOrConnected) {
            connectingOrConnected = false;
        }

        if (socket != null && socket.connected()) {
            socket.off();
            socket.disconnect();

            while (socket.connected()) {
                try {
                    Thread.sleep(100);
                    Log.d("INFO", "Waiting for Stop Socket!!");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    @Override
    public boolean isConnectingOrConnected() {
        return connectingOrConnected;
    }

}
