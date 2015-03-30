package com.tosslab.jandi.app.network.socket;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.tosslab.jandi.app.JandiConstantsForFlavors;

import java.net.URISyntaxException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Steve SeongUg Jung on 15. 3. 30..
 */
public class JandiSocketManager {
    private static JandiSocketManager jandiSocketManager;
    private ConnectState connectState = ConnectState.Disconnected;
    private Socket socket;


    private JandiSocketManager() {
    }

    public static JandiSocketManager getInstance() {
        if (jandiSocketManager == null) {
            jandiSocketManager = new JandiSocketManager();
        }

        return jandiSocketManager;
    }

    synchronized public ConnectState connect(AsyncHttpClient.WebSocketConnectCallback connectCallback) {
        if (connectState != ConnectState.Disconnected) {
            return connectState;
        }

        if (socket == null) {
            try {
                IO.Options options = new IO.Options();
                options.reconnection = true;

                socket = IO.socket(JandiConstantsForFlavors.SERVICE_ROOT_URL, options);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                connectState = ConnectState.Disconnected;
                return connectState;
            }
        }

        if (socket != null) {

            register(Socket.EVENT_CONNECT, o -> connectState = ConnectState.Connected);
            register(Socket.EVENT_CONNECT_ERROR, o -> connectState = ConnectState.Disconnected);
            register(Socket.EVENT_CONNECT_TIMEOUT, o -> connectState = ConnectState.Disconnected);
            register(Socket.EVENT_DISCONNECT, o -> connectState = ConnectState.Disconnected);
            register("hello", o -> connectState = ConnectState.Connected);

            connectState = ConnectState.Connecting;
            socket.connect();

        }

        return connectState;
    }

    synchronized public void disconnect() {

        if (socket != null && socket.connected()) {
            socket.disconnect();
        }
    }

    public void register(String event, final Action1<Object> onNext, final Action1<Throwable> onError) {
        if (socket != null && !socket.hasListeners(event)) {
            socket.on(event, args -> Observable.from(args).observeOn(AndroidSchedulers.mainThread()).subscribe(onNext, onError));
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

    private enum ConnectState {
        Connecting, Disconnected, Connected
    }
}
