package com.tosslab.jandi.app.ui.maintab.tabs.topic.domain;

/**
 * Created by tee on 2017. 2. 10..
 */

public class _TopicItemData implements IMarkerTopicFolderItem {
    private long creatorId;
    private long parentId;
    private String name;
    private boolean isStarred;
    private boolean isJoined;
    private long entityId;
    private int memberCount;
    private int unreadCount;
    private boolean isPublic;
    private String description;
    private boolean selected;
    private long markerLinkId;
    private boolean isPushOn;
    private boolean isReadOnly;
    private int childIndex;
    private int parentChildCnt;
    private boolean isInnerFolder;

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public boolean isInnerFolder() {
        return isInnerFolder;
    }

    public void setInnerFolder(boolean innerFolder) {
        isInnerFolder = innerFolder;
    }

    public int getParentChildCnt() {
        return parentChildCnt;
    }

    public void setParentChildCnt(int parentChildCnt) {
        this.parentChildCnt = parentChildCnt;
    }

    public int getChildIndex() {
        return childIndex;
    }

    public void setChildIndex(int childIndex) {
        this.childIndex = childIndex;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setStarred(boolean starred) {
        isStarred = starred;
    }

    public boolean isJoined() {
        return isJoined;
    }

    public void setJoined(boolean joined) {
        isJoined = joined;
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public long getMarkerLinkId() {
        return markerLinkId;
    }

    public void setMarkerLinkId(long markerLinkId) {
        this.markerLinkId = markerLinkId;
    }

    public boolean isPushOn() {
        return isPushOn;
    }

    public void setPushOn(boolean pushOn) {
        isPushOn = pushOn;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
    }

    @Override
    public String toString() {
        return "_TopicItemData{" +
                "creatorId=" + creatorId +
                ", name='" + name + '\'' +
                ", isStarred=" + isStarred +
                ", isJoined=" + isJoined +
                ", entityId=" + entityId +
                ", memberCount=" + memberCount +
                ", unreadCount=" + unreadCount +
                ", isPublic=" + isPublic +
                ", description='" + description + '\'' +
                ", selected=" + selected +
                ", markerLinkId=" + markerLinkId +
                ", isPushOn=" + isPushOn +
                ", isReadOnly=" + isReadOnly +
                '}';
    }
}
