package com.tosslab.jandi.app.network.socket;

import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.tosslab.jandi.app.JandiConstantsForFlavors;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Steve SeongUg Jung on 15. 3. 30..
 */
public class JandiSocketManager {
    private static JandiSocketManager jandiSocketManager;
    private Socket socket;

    private Map<String, Subscription> eventSubscriber;


    private JandiSocketManager() {
        eventSubscriber = new HashMap<String, Subscription>();
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
                removeSubscription(event);
                Subscription subscribe = Observable.from(args).observeOn(AndroidSchedulers.mainThread()).subscribe(onNext, onError);
                eventSubscriber.put(event, subscribe);
            });
        }
    }

    public void register(String event, final Action1<Object> onNext) {
        register(event, onNext, throwable -> {
        });
    }

    public void unregister(String event) {
        if (socket.hasListeners(event)) {
            removeSubscription(event);
            socket.off(event);
        }
    }

    private void removeSubscription(String event) {
        if (eventSubscriber.containsKey(event)) {
            eventSubscriber.get(event).unsubscribe();
            eventSubscriber.remove(event);
        }
    }

}
