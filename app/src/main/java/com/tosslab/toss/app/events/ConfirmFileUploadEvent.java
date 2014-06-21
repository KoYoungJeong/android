package com.tosslab.toss.app.events;

/**
 * Created by justinygchoi on 2014. 6. 21..
 */
public class ConfirmFileUploadEvent {
    public String realFilePath;
    public ConfirmFileUploadEvent(String realFilePath) {
        this.realFilePath = realFilePath;
    }
}
