package com.tosslab.jandi.app.network.socket.emit;

import com.tosslab.jandi.app.network.json.JsonMapper;

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
            String jsonData;
            if (data instanceof String) {
                jsonData = (String) data;
            } else {
                jsonData = JsonMapper.getInstance().getObjectMapper().writeValueAsString(data);
            }
            emitter.emit(socketEmitData.getEvent(), jsonData);
            // 소켓이 중지팝업등으로 끊길 경우 이벤트 히스토리 타임을 저장하기 위해 emit시 그때그때 시간을 저장해 둠
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
