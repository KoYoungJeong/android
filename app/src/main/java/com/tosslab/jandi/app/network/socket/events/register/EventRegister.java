package com.tosslab.jandi.app.network.socket.events.register;

import com.tosslab.jandi.app.network.socket.events.EventListener;

import io.socket.emitter.Emitter;

/**
 * Created by Steve SeongUg Jung on 15. 4. 2..
 */
public interface EventRegister {

    void register(String event, EventListener eventListener);

    void unregister(String event, EventListener eventListener);

    void setEmitter(Emitter socket);
}
