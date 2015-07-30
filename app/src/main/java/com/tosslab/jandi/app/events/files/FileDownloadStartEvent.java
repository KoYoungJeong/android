package com.tosslab.jandi.app.events.files;

/**
 * Created by Steve SeongUg Jung on 15. 1. 8..
 */
public class FileDownloadStartEvent {

    private String url;
    private String fileName;
    private String fileType;
    private final String ext;

    public FileDownloadStartEvent(String url, String fileName, String fileType, String ext) {
        this.url = url;
        this.fileName = fileName;
        this.fileType = fileType;
        this.ext = ext;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getExt() {
        return ext;
    }
}
