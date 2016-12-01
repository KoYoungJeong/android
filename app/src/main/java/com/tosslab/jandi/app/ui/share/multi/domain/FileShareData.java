package com.tosslab.jandi.app.ui.share.multi.domain;

import java.io.File;

public class FileShareData implements ShareData {
    private String filePath;
    private String fileName;

    public FileShareData(String filePath) {
        this.filePath = filePath;
        this.fileName = getFileNameByPath(filePath);
    }

    public String getFileNameByPath(String data) {
        return new File(data).getName();
    }

    @Override
    public String getData() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
