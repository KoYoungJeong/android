package com.tosslab.jandi.app.network.socket.connector;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.tosslab.jandi.app.network.socket.events.EventListener;
import com.tosslab.jandi.app.utils.logger.LogUtil;

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
                options.timeout = 1000 * 10;
                socket = IO.socket(url, options);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        if (socket != null) {

            socket.on(Socket.EVENT_CONNECT, args -> LogUtil.e(TAG, Socket.EVENT_CONNECT))
                    .on(Socket.EVENT_ERROR, args -> {
                        LogUtil.e(TAG, Socket.EVENT_ERROR);
                        disconnectCallback(disconnectListener, args);
                    })
                    .on(Socket.EVENT_DISCONNECT, args -> {
                        LogUtil.e(TAG, Socket.EVENT_DISCONNECT);
                        disconnectCallback(disconnectListener, args);
                    })
                    .on(Socket.EVENT_CONNECT_ERROR, args -> {
                        LogUtil.e(TAG, Socket.EVENT_CONNECT_ERROR);
                        disconnectCallback(disconnectListener, args);
                    })
                    .on(Socket.EVENT_CONNECT_TIMEOUT, args -> {
                        LogUtil.e(TAG, Socket.EVENT_CONNECT_TIMEOUT);
                        disconnectCallback(disconnectListener, args);
                    });

            socket.connect();
            connectingOrConnected = true;
        }

        return socket;
    }

    public static final String TAG = "SocketConnector";

    private void disconnectCallback(EventListener disconnectListener, Object[] args) {
        if (args != null) {
            for (Object arg : args) {
                LogUtil.e(TAG, "Disconnect Reason : " + arg.toString());
            }
        }

        connectingOrConnected = false;
        if (disconnectListener != null) {
            disconnectListener.callback(args);
        }
    }

    @Override
    public void disconnect() {
        connectingOrConnected = false;

        if (socket != null && socket.connected()) {
            socket.off();
            socket.disconnect();

            while (socket.connected()) {
                try {
                    Thread.sleep(100);
                    LogUtil.d(TAG, "Socket Stopping...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean isConnectingOrConnected() {
        return (connectingOrConnected = socket != null && socket.connected());
    }

}
