package com.tosslab.jandi.app.ui.maintab.tabs.topic.domain;

/**
 * Created by tee on 15. 8. 27..
 */
public class TopicFolderData {

    private final long id;
    private long nextChildId;
    private long childBadgeCnt = 0;
    private int itemCount = 0;
    private int seq;
    private String title;
    private long folderId;
    private boolean isFakeFolder = false;

    public TopicFolderData(long id, String title, long folderId) {
        this.id = id;
        this.title = title;
        this.folderId = folderId;
        nextChildId = 0;
    }

    public long getGroupId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getFolderId() {
        return folderId;
    }

    public long getChildBadgeCnt() {
        return childBadgeCnt;
    }

    public void setChildBadgeCnt(long count) {
        this.childBadgeCnt = count;
    }

    public void setIsFakeFolder(boolean isFakeFolder) {
        this.isFakeFolder = isFakeFolder;
    }

    public boolean isFakeFolder() {
        return isFakeFolder;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public long generateNewChildId() {
        final long id = nextChildId;
        nextChildId += 1;
        return id;
    }

}
