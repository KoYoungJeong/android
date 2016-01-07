package com.tosslab.jandi.app.events;

/**
 * Created by tee on 16. 1. 7..
 */
public class DefaultProfileChangeEvent {

    private int mode;
    private Object data;

    public DefaultProfileChangeEvent(int mode, Object data) {
        this.mode = mode;
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public int getMode() {
        return mode;
    }

}
