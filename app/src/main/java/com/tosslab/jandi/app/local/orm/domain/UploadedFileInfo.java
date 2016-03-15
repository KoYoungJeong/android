package com.tosslab.jandi.app.local.orm.domain;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "uploaded_file_info")
public class UploadedFileInfo {
    @DatabaseField(id = true)
    private long messageId;
    @DatabaseField(defaultValue = "")
    private String localPath;

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
}
