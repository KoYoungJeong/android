package com.tosslab.jandi.app.network.socket.emit;

import com.tosslab.jandi.app.network.json.JacksonMapper;

import java.io.IOException;

import io.socket.emitter.Emitter;

/**
 * Created by Steve SeongUg Jung on 15. 4. 2..
 */
public class JsonSocketEmitter implements SocketEmitter {

    private Emitter emitter;

    @Override
    public void setEmitter(Emitter emitter) {
        this.emitter = emitter;
    }

    public <T> void emit(SocketEmitData<T> socketEmitData) {

        T data = socketEmitData.getObject();

        try {
            String jsonData = JacksonMapper.getInstance().getObjectMapper().writeValueAsString(data);
            emitter.emit(socketEmitData.getEvent(), jsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
