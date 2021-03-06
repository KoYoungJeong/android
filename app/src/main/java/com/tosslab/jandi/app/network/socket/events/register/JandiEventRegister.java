package com.tosslab.jandi.app.network.socket.events.register;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.socket.events.EventListener;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.socket.emitter.Emitter;

/**
 * Created by Steve SeongUg Jung on 15. 4. 2..
 */
public class JandiEventRegister implements EventRegister {
    public static final String TAG = "SocketEventRegister";

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
                    LogUtil.d(TAG, event);
//                    LogUtil.d(TAG, " = " + args[0].toString());
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

    private void registerIfPending() {
        for (String event : eventMapper.keySet()) {
            if (socket != null && !socket.hasListeners(event)) {
                socket.on(event, args -> {
                    if (args != null && args[0] != null) {
                        LogUtil.d(TAG, event);
//                      LogUtil.d(TAG, " = " + args[0].toString());
                    }
                    for (EventListener listener : eventMapper.get(event)) {
                        listener.callback(args);
                    }
                });
            }
        }
    }
}
