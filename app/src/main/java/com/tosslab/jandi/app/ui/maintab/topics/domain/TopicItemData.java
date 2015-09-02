package com.tosslab.jandi.app.ui.maintab.topics.domain;

import com.tosslab.jandi.app.lists.libs.advancerecyclerview.provider.AbstractExpandableDataProvider;

/**
 * Created by tee on 15. 8. 27..
 */
public class TopicItemData extends AbstractExpandableDataProvider.ChildData {

    private final int swipeReaction;
    private long id;

    private int creatorId;
    private boolean pinnedToSwipeLeft;
    private String name;
    private boolean isStarred;
    private boolean isJoined;
    private int entityId;
    private int memberCount;
    private int unreadCount;
    private boolean isPublic;
    private String description;
    private boolean selected;
    private int markerLinkId;
    private boolean isPushOn;
//    private boolean isFakeJoinButton;

    public TopicItemData() {
        swipeReaction = 0;
    }

    public TopicItemData(long id, int swipeReaction, int creatorId, String name, boolean isStarred,
                         boolean isJoined, int entityId, int unreadCount, int markerLinkId, boolean isPushOn,
                         boolean selected, String description, boolean isPublic, int memberCount) {
        this.swipeReaction = swipeReaction;
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
    }

    public static TopicItemData newInstance(long id, int swipeReaction, int creatorId, String name,
                                            boolean isStarred, boolean isJoined, int entityId,
                                            int unreadCount, int markerLinkId, boolean isPushOn,
                                            boolean selected, String description, boolean isPublic,
                                            int memberCount) {

        return new TopicItemData(id, swipeReaction, creatorId, name, isStarred, isJoined, entityId,
                unreadCount, markerLinkId, isPushOn, selected, description, isPublic, memberCount);

    }

    // Topic Join Button 추가를 위한 더미 데이터
    public static TopicItemData getDummyInstance() {
        return new TopicItemData();
    }

    @Override
    public boolean isPinnedToSwipeLeft() {
        return pinnedToSwipeLeft;
    }

    @Override
    public void setPinnedToSwipeLeft(boolean pinnedToSwipeLeft) {
        this.pinnedToSwipeLeft = pinnedToSwipeLeft;
    }

    @Override
    public long getChildId() {
        return id;
    }

    public void setChildId(long id) {
        this.id = id;
    }

    @Override
    public int getSwipeReactionType() {
        return swipeReaction;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
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

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
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

    public int getMarkerLinkId() {
        return markerLinkId;
    }

    public void setMarkerLinkId(int markerLinkId) {
        this.markerLinkId = markerLinkId;
    }

    public boolean isPushOn() {
        return isPushOn;
    }

    public void setIsPushOn(boolean isPushOn) {
        this.isPushOn = isPushOn;
    }

//    public boolean isFakeJoinButton() {
//        return isFakeJoinButton;
//    }
//
//    public void setIsFakeJoinButton(boolean isFakeJoinButton) {
//        this.isFakeJoinButton = isFakeJoinButton;
//    }
}
