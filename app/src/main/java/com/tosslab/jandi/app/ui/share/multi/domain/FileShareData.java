package com.tosslab.jandi.app.ui.share.multi.domain;

import com.tosslab.jandi.app.utils.file.FileUtil;

public class FileShareData implements ShareData {
    private String filePath;
    private String fileName;

    public FileShareData(String filePath) {
        this.filePath = filePath;
        this.fileName = getFileNameByPath(filePath);
    }

    public String getFileNameByPath(String filePath) {
        int lastIndexOf = filePath.lastIndexOf("/");
        String originName = filePath.substring(lastIndexOf + 1);
        return FileUtil.convertAvailableFileName(originName);
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
