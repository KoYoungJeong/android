package com.tosslab.jandi.app.services.download.domain;

public class DownloadFileInfo {
    final long fileId;
    final String fileUrl;
    final String fileName;
    final String fileExt;
    final String fileType;

    public DownloadFileInfo(long fileId, String fileUrl, String fileName, String fileExt, String fileType) {
        this.fileId = fileId;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileExt = fileExt;
        this.fileType = fileType;
    }

    public long getFileId() {
        return fileId;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileExt() {
        return fileExt;
    }

    public String getFileType() {
        return fileType;
    }
}
