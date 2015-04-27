package com.tosslab.jandi.app.network.socket.emit;

/**
 * Created by Steve SeongUg Jung on 15. 4. 2..
 */
public class SocketEmitData<T> {
    private String event;
    private T object;

    public SocketEmitData(String event, T object) {

        this.event = event;
        this.object = object;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }
}
