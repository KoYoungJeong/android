package com.tosslab.jandi.app.ui.maintab.tabs.topic.domain;

/**
 * Created by tee on 15. 8. 27..
 */
public class TopicItemData {

    private long id;

    private long creatorId;
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

    private TopicItemData() {
    }

    private TopicItemData(long id, long creatorId, String name, boolean isStarred,
                          boolean isJoined, long entityId, int unreadCount, long markerLinkId, boolean isPushOn,
                          boolean selected, String description, boolean isPublic, int memberCount, boolean isReadOnly) {
        this.id = id;
        this.creatorId = creatorId;
        this.name = name;
        this.isStarred = isStarred;
        this.isJoined = isJoined;
        this.entityId = entityId;
        this.unreadCount = unreadCount;
        this.markerLinkId = markerLinkId;
        this.isPushOn = isPushOn;
        this.selected = selected;
        this.description = description;
        this.isPublic = isPublic;
        this.memberCount = memberCount;
        this.isReadOnly = isReadOnly;
    }

    public static TopicItemData newInstance(long id, long creatorId, String name,
                                            boolean isStarred, boolean isJoined, long entityId,
                                            int unreadCount, long markerLinkId, boolean isPushOn,
                                            boolean selected, String description, boolean isPublic,
                                            int memberCount, boolean isReadOnly) {

        return new TopicItemData(id, creatorId, name, isStarred, isJoined, entityId,
                unreadCount, markerLinkId, isPushOn, selected, description, isPublic, memberCount, isReadOnly);

    }

    // Topic Join Button 추가를 위한 더미 데이터
    public static TopicItemData getDummyInstance() {
        return new TopicItemData();
    }

    public long getChildId() {
        return id;
    }

    public void setChildId(long id) {
        this.id = id;
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

    public void setIsStarred(boolean isStarred) {
        this.isStarred = isStarred;
    }

    public boolean isJoined() {
        return isJoined;
    }

    public void setIsJoined(boolean isJoined) {
        this.isJoined = isJoined;
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

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
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

    public void setIsPushOn(boolean isPushOn) {
        this.isPushOn = isPushOn;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
    }
}
