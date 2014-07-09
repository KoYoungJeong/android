package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 6. 21..
 */
public class ConfirmFileUploadEvent {
    public String realFilePath;
    public int cdpId;
    public String comment;
    public ConfirmFileUploadEvent(int cdpId, String realFilePath, String comment) {
        this.cdpId = cdpId;
        this.realFilePath = realFilePath;
        this.comment = comment;
    }
}
