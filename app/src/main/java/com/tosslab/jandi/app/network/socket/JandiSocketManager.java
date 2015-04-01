package com.tosslab.jandi.app.network.socket;

import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.socket.connector.SocketConnector;

import java.net.URISyntaxException;

import rx.functions.Action1;

/**
 * Created by Steve SeongUg Jung on 15. 3. 30..
 */
public class JandiSocketManager {
    private static JandiSocketManager jandiSocketManager;

    private SocketConnector socketConnector;

    private Socket socket;

    private JandiSocketManager() {
    }

    public static JandiSocketManager getInstance() {
        if (jandiSocketManager == null) {
            jandiSocketManager = new JandiSocketManager();
        }

        return jandiSocketManager;
    }

    synchronized public boolean connect() {
        if (socket != null && socket.connected()) {
            return true;
        }

        if (socket == null) {
            try {
                IO.Options options = new IO.Options();
                options.reconnection = true;

                socket = IO.socket(JandiConstantsForFlavors.SERVICE_ROOT_URL, options);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return false;
            }
        }

        if (socket != null) {

            register(Socket.EVENT_CONNECT, o -> Log.d("INFO", String.valueOf(o)));
            register(Socket.EVENT_DISCONNECT, o -> Log.d("INFO", String.valueOf(o)));
            register("hello", o -> Log.d("INFO", String.valueOf(o)));

            socket.connect();

        }

        return true;
    }

    synchronized public void disconnect() {

        if (socket != null && socket.connected()) {
            socket.disconnect();
        }
    }

    public void register(String event, final Action1<Object> onNext, final Action1<Throwable> onError) {
        if (socket != null && !socket.hasListeners(event)) {
            socket.on(event, args -> {

            });
        }
    }

    public void register(String event, final Action1<Object> onNext) {
        register(event, onNext, throwable -> {
        });
    }

    public void unregister(String event) {
        if (socket.hasListeners(event)) {
            socket.off(event);
        }
    }


}
