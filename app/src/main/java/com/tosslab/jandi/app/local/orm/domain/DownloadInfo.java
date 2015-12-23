package com.tosslab.jandi.app.local.orm.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "download_info")
public class DownloadInfo {
    @DatabaseField(id = true)
    private int notificationId;
    @DatabaseField
    private String fileName;
    /**
     * 0 = inprogress, 1 = complete, -1 = fail
     */
    @DatabaseField
    private int state;

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    /**
     * @return 0 = inprogress, 1 = complete, -1 = fail
     */
    public int getState() {
        return state;
    }

    /**
     * @param state 0 = inprogress, 1 = complete, -1 = fail
     */
    public void setState(int state) {
        this.state = state;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
