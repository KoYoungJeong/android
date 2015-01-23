package com.tosslab.jandi.app.ui.fileexplorer.to;

/**
 * Created by Steve SeongUg Jung on 15. 1. 23..
 */
public class FileItem {

    private final String name;
    private final String path;
    private final int childCount;
    private final String modifiedDate;
    private final boolean isDirectory;

    public FileItem(String name, String path, int childCount, String modifiedDate, boolean isDirectory) {
        this.name = name;
        this.path = path;
        this.childCount = childCount;
        this.modifiedDate = modifiedDate;
        this.isDirectory = isDirectory;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public int getChildCount() {
        return childCount;
    }
}
