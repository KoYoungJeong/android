package com.tosslab.jandi.app.network.socket.events.register;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.tosslab.jandi.app.network.socket.events.EventListener;

/**
 * Created by Steve SeongUg Jung on 15. 4. 2..
 */
public interface EventRegister {

    void register(String event, EventListener eventListener);

    void unregister(String event, EventListener eventListener);

    void setEmitter(Emitter socket);
}
