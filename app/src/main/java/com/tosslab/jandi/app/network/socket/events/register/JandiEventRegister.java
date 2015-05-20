package com.tosslab.jandi.app.network.socket.events.register;

import android.text.TextUtils;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.tosslab.jandi.app.network.socket.events.EventListener;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Steve SeongUg Jung on 15. 4. 2..
 */
public class JandiEventRegister implements EventRegister {

    private Emitter socket;

    private Map<String, Queue<EventListener>> eventMapper;

    public JandiEventRegister() {
        this.eventMapper = new ConcurrentHashMap<String, Queue<EventListener>>();
    }

    @Override
    public void register(String event, EventListener eventListener) {

        if (TextUtils.isEmpty(event) || eventListener == null) {
            return;
        }

        if (eventMapper.containsKey(event)) {
            if (!eventMapper.get(event).contains(eventListener)) {
                eventMapper.get(event).offer(eventListener);
            }
        } else {
            Queue<EventListener> eventListeners = new ConcurrentLinkedQueue<EventListener>();
            eventListeners.offer(eventListener);
            eventMapper.put(event, eventListeners);
        }

        if (socket != null && !socket.hasListeners(event)) {
            socket.on(event, args -> {
                if (args != null && args[0] != null) {
                    Log.d("INFO", event + " = " + args[0].toString());
                }
                for (EventListener listener : eventMapper.get(event)) {
                    listener.callback(args);
                }
            });
        }
    }

    @Override
    public void unregister(String event, EventListener eventListener) {
        if (eventMapper.containsKey(event)) {
            Queue<EventListener> eventListeners = eventMapper.get(event);
            if (eventListeners.contains(eventListener)) {
                eventListeners.remove(eventListener);
            }
        }
    }

    @Override
    public void setEmitter(Emitter socket) {
        this.socket = socket;
        registerIfPending();
    }

    public static final String TAG = "SocketEventRegister";
    private void registerIfPending() {
        for (String event : eventMapper.keySet()) {
            Log.d(TAG, event);
            if (socket != null && !socket.hasListeners(event)) {
                socket.on(event, args -> {
                    for (EventListener listener : eventMapper.get(event)) {
                        listener.callback(args);
                    }
                });
            }
        }
    }
}
