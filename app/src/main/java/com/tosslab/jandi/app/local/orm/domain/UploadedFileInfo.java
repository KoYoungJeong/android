package com.tosslab.jandi.app.local.orm.domain;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "uploaded_file_info")
public class UploadedFileInfo {

    @DatabaseField(id = true)
    private int messageId;
    @DatabaseField(defaultValue = "")
    private String localPath;

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
}
