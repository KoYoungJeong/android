package com.tosslab.jandi.app.ui.share.multi.domain;

public class FileShareData implements ShareData {
    private String filePath;

    public FileShareData(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String getData() {
        return filePath;
    }
}
