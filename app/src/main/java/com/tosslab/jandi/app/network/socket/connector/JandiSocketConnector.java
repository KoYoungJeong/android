package com.tosslab.jandi.app.network.socket.connector;

import android.os.SystemClock;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.tosslab.jandi.app.network.socket.events.EventListener;

import org.apache.log4j.Logger;

import java.net.URISyntaxException;

/**
 * Created by Steve SeongUg Jung on 15. 4. 1..
 */
public class JandiSocketConnector implements SocketConnector {

    private static final Logger logger = Logger.getLogger(JandiSocketConnector.class);


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
                options.timeout = 1000 * 8;
                socket = IO.socket(url, options);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        if (socket != null) {

            socket.on(Socket.EVENT_CONNECT, args -> Log.e(TAG, Socket.EVENT_CONNECT))
                    .on(Socket.EVENT_ERROR, args -> Log.e(TAG, Socket.EVENT_ERROR))
                    .on(Socket.EVENT_DISCONNECT, args ->
                            disconnectCallback(disconnectListener, args))
                    .on(Socket.EVENT_CONNECT_ERROR, args ->
                            disconnectCallback(disconnectListener, args))
                    .on(Socket.EVENT_CONNECT_TIMEOUT, args ->
                            disconnectCallback(disconnectListener, args));

            socket.connect();
            connectingOrConnected = true;
        }

        return socket;
    }


    public static final String TAG = "SocketConnector";
    private void disconnectCallback(EventListener disconnectListener, Object[] args) {

        logger.debug("Disconnect");
        Log.e(TAG, "Disconnect");
        if (args != null) {
            for (Object arg : args) {
                logger.debug("Disconnect Reason : " + arg.toString());
                Log.e(TAG, "Disconnect Reason : " + arg.toString());
            }
        }

        connectingOrConnected = false;
        if (disconnectListener != null) {
            disconnectListener.callback(args);
        }
    }

    @Override
    public void disconnect() {

//        if (connectingOrConnected) {
            connectingOrConnected = false;
//        }

        if (socket != null && socket.connected()) {
//            Log.e(TAG, "disconnect");
            socket.off();
            socket.disconnect();

            while (socket.connected()) {
                try {
                    Thread.sleep(100);
                    Log.d(TAG, "Waiting for Stop Socket!!");
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
