package com.tosslab.jandi.app.ui.maintab.tabs.topic.domain;

/**
 * Created by tee on 2017. 2. 10..
 */

public class _TopicFolderData implements IMarkerTopicFolderItem {
    private long childBadgeCnt = 0;
    private int itemCount = 0;
    private int seq;
    private String title;
    private long folderId;
    private boolean isOpened;

    public boolean isOpened() {
        return isOpened;
    }

    public void setOpened(boolean opened) {
        isOpened = opened;
    }

    public long getChildBadgeCnt() {
        return childBadgeCnt;
    }

    public void setChildBadgeCnt(long childBadgeCnt) {
        this.childBadgeCnt = childBadgeCnt;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getFolderId() {
        return folderId;
    }

    public void setFolderId(long folderId) {
        this.folderId = folderId;
    }

    @Override
    public String toString() {
        return "_TopicFolderData{" +
                "childBadgeCnt=" + childBadgeCnt +
                ", itemCount=" + itemCount +
                ", seq=" + seq +
                ", title='" + title + '\'' +
                ", folderId=" + folderId +
                '}';
    }
}
