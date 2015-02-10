package com.tosslab.jandi.app.events.files;

/**
 * Created by justinygchoi on 2014. 6. 21..
 */
public class ConfirmFileUploadEvent {
    public String title;
    public String realFilePath;
    public int entityId;
    public String comment;
    public ConfirmFileUploadEvent(String title, int entityId, String realFilePath, String comment) {
        this.title = title;
        this.entityId = entityId;
        this.realFilePath = realFilePath;
        this.comment = comment;
    }
}
