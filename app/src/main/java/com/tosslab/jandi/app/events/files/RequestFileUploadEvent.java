package com.tosslab.jandi.app.events.files;

/**
 * Created by justinygchoi on 2014. 6. 23..
 */
public class RequestFileUploadEvent {
    public int type;

    public RequestFileUploadEvent(int type) {
        this.type = type;
    }
}
