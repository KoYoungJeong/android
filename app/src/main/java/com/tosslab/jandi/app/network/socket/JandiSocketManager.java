package com.tosslab.jandi.app.network.socket;

import android.support.annotation.NonNull;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.socket.connector.JandiSocketConnector;
import com.tosslab.jandi.app.network.socket.connector.SocketConnector;
import com.tosslab.jandi.app.network.socket.emit.JsonSocketEmitter;
import com.tosslab.jandi.app.network.socket.emit.SocketEmitData;
import com.tosslab.jandi.app.network.socket.emit.SocketEmitter;
import com.tosslab.jandi.app.network.socket.events.EventListener;
import com.tosslab.jandi.app.network.socket.events.register.EventRegister;
import com.tosslab.jandi.app.network.socket.events.register.JandiEventRegister;

import io.socket.emitter.Emitter;

/**
 * Created by Steve SeongUg Jung on 15. 3. 30..
 */
public class JandiSocketManager {
    private static JandiSocketManager jandiSocketManager;

    private SocketConnector socketConnector;
    private EventRegister eventRegister;
    private SocketEmitter jsonSocketEmitter;


    private JandiSocketManager() {
        socketConnector = new JandiSocketConnector();
        eventRegister = new JandiEventRegister();
        jsonSocketEmitter = new JsonSocketEmitter();
    }

    public static JandiSocketManager getInstance() {
        if (jandiSocketManager == null) {
            jandiSocketManager = new JandiSocketManager();
        }

        return jandiSocketManager;
    }

    synchronized public boolean connect(EventListener disconnectListener) {
        Emitter emitter = socketConnector.connect(JandiConstantsForFlavors.getSocketRootUrl(), disconnectListener);
        eventRegister.setEmitter(emitter);
        jsonSocketEmitter.setEmitter(emitter);
        return true;
    }

    synchronized public void disconnect() {
        socketConnector.disconnect();
    }

    public <T> void sendByJson(String event, @NonNull T object) {
        SocketEmitData socketEmitData = new SocketEmitData(event, object);
        jsonSocketEmitter.emit(socketEmitData);
    }

    synchronized public void register(String event, EventListener eventListener) {
        eventRegister.register(event, eventListener);
    }

    synchronized public void unregister(String event, EventListener eventListener) {
        eventRegister.unregister(event, eventListener);
    }

    public boolean isConnectingOrConnected() {
        return socketConnector.isConnectingOrConnected();
    }

    public void release() {
        jandiSocketManager = null;
    }
}
