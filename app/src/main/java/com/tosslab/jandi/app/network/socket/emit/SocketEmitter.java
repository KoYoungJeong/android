package com.tosslab.jandi.app.network.socket.emit;


import io.socket.emitter.Emitter;

/**
 * Created by Steve SeongUg Jung on 15. 4. 2..
 */
public interface SocketEmitter {
    void setEmitter(Emitter emitter);

    <T> void emit(SocketEmitData<T> socketEmitData);
}
