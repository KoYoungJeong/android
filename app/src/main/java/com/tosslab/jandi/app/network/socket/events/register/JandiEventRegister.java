package com.tosslab.jandi.app.network.socket.events.register;

import android.text.TextUtils;

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
            if (eventMapper.get(event).contains(eventListener)) {
            } else {
                eventMapper.get(event).offer(eventListener);
            }
            return;
        } else {
            Queue<EventListener> eventListeners = new ConcurrentLinkedQueue<EventListener>();
            eventListeners.offer(eventListener);
            eventMapper.put(event, eventListeners);
        }

        if (socket != null) {
            socket.on(event, args -> {
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
    }
}
