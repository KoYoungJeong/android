package com.tosslab.jandi.app.network.models;

/**
 * Created by tee on 15. 8. 25..
 */
public class ResCreateFolder {

    private int folderId;
    private String folderName;

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    @Override
    public String toString() {
        return "ResCreateFolder{" +
                "folderId=" + folderId +
                ", folderName='" + folderName + '\'' +
                '}';
    }
}
