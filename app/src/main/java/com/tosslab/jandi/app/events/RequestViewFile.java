package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 7. 17..
 */
public class RequestViewFile {
    public String fileUrl;
    public String fileType;
    public RequestViewFile(String fileUrl, String fileType) {
        this.fileUrl = fileUrl;
        this.fileType = fileType;
    }
}
