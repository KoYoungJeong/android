package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 7. 17..
 */
public class RequestViewFile {
    public String fileUrl;
    public String fileName;
    public RequestViewFile(String fileUrl, String fileName) {
        this.fileUrl = fileUrl;
        this.fileName = fileName;
    }
}
