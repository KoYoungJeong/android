package com.tosslab.jandi.app.network.socket.events.register;

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

    private Socket socket;

    private Map<String, List<EventListener>> eventMapper;

    public JandiEventRegister(Socket socket) {
        this.socket = socket;
        this.eventMapper = new HashMap<String, List<EventListener>>();
    }

    @Override
    public void register(String event, EventListener eventListener) {

        if (eventMapper.containsKey(event)) {
            if (eventMapper.get(event).contains(eventListener)) {
                return;
            }
        } else {
            List<EventListener> eventListeners = new ArrayList<>();
            eventListeners.add(eventListener);
            eventMapper.put(event, eventListeners);
        }

        socket.on(event, args -> eventListener.callback(args));
    }

    @Override
    public void unregister(String event, EventListener eventListener) {
        if (eventMapper.containsKey(event)) {
            eventMapper.get(event).remove(eventListener);
        }
    }
}
