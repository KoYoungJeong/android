package com.tosslab.jandi.app.network.socket.events.register;

import android.text.TextUtils;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.tosslab.jandi.app.network.socket.events.EventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Steve SeongUg Jung on 15. 4. 2..
 */
public class JandiEventRegister implements EventRegister {

    private Emitter socket;

    private Map<String, List<EventListener>> eventMapper;

    public JandiEventRegister() {
        this.eventMapper = new HashMap<String, List<EventListener>>();
    }

    @Override
    public void register(String event, EventListener eventListener) {

        if (TextUtils.isEmpty(event) || eventListener == null) {
            return;
        }

        if (eventMapper.containsKey(event)) {
            if (eventMapper.get(event).contains(eventListener)) {
                return;
            }
        } else {
            List<EventListener> eventListeners = new ArrayList<>();
            eventListeners.add(eventListener);
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
            List<EventListener> eventListeners = eventMapper.get(event);
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
