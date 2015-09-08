package com.tosslab.jandi.app.ui.maintab.topic.domain;

import com.tosslab.jandi.app.lists.libs.advancerecyclerview.provider.AbstractExpandableDataProvider;

/**
 * Created by tee on 15. 8. 27..
 */
public class TopicFolderData extends AbstractExpandableDataProvider.GroupData {

    private final long id;
    private final int swipeReaction;
    private long nextChildId;

    private long childBadgeCnt = 0;
    private int itemCount = 0;
    private int seq;
    private String title;
    private int folderId;
    private boolean pinnedToSwipeLeft;
    private boolean isFakeFolder = false;

    public TopicFolderData(long id, String title, int folderId, int swipeReaction) {
        this.id = id;
        this.title = title;
        this.folderId = folderId;
        this.swipeReaction = swipeReaction;
        nextChildId = 0;
    }

    @Override
    public long getGroupId() {
        return id;
    }

    @Override
    public int getSwipeReactionType() {
        return swipeReaction;
    }

    @Override
    public boolean isPinnedToSwipeLeft() {
        return pinnedToSwipeLeft;
    }

    @Override
    public void setPinnedToSwipeLeft(boolean pinnedToSwipeLeft) {
        this.pinnedToSwipeLeft = pinnedToSwipeLeft;
    }

    public String getTitle() {
        return title;
    }

    public int getFolderId() {
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
