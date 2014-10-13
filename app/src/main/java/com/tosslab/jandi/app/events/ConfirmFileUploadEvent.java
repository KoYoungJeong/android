package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 6. 21..
 */
public class ConfirmFileUploadEvent {
    public String realFilePath;
    public int entityId;
    public String comment;
    public ConfirmFileUploadEvent(int entityId, String realFilePath, String comment) {
        this.entityId = entityId;
        this.realFilePath = realFilePath;
        this.comment = comment;
    }
}
