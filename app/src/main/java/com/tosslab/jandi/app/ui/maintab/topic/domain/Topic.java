package com.tosslab.jandi.app.ui.maintab.topic.domain;

/**
 * Created by Steve SeongUg Jung on 15. 7. 7..
 */
public class Topic {

    private final int creatorId;
    private String name;
    private boolean isStarred;
    private boolean isJoined;
    private int entityId;
    private int memberCount;
    private int unreadCount;
    private boolean isPublic;
    private String description;
    private boolean selected;

    public Topic(String name, boolean isStarred, boolean isJoined, int entityId, int memberCount, int unreadCount, boolean isPublic, String description, int creatorId) {
        this.name = name;
        this.isStarred = isStarred;
        this.isJoined = isJoined;
        this.entityId = entityId;
        this.memberCount = memberCount;
        this.unreadCount = unreadCount;
        this.isPublic = isPublic;
        this.description = description;
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

    public int getCreatorId() {
        return creatorId;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public static class Builder {
        private String name;
        private boolean isStarred;
        private boolean isJoined;
        private int entityId;
        private int memberCount;
        private int unreadCount;
        private boolean isPublic;
        private String description;
        private int creatorId;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder isStarred(boolean isStarred) {
            this.isStarred = isStarred;
            return this;
        }

        public Builder isJoined(boolean isJoined) {
            this.isJoined = isJoined;
            return this;
        }

        public Builder entityId(int entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder memberCount(int memberCount) {
            this.memberCount = memberCount;
            return this;
        }

        public Builder unreadCount(int unreadCount) {
            this.unreadCount = unreadCount;
            return this;
        }

        public Builder isPublic(boolean isPublic) {
            this.isPublic = isPublic;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Topic build() {
            return new Topic(name, isStarred, isJoined, entityId, memberCount, unreadCount,
                    isPublic, description, creatorId);
        }

        public Builder creatorId(int creatorId) {
            this.creatorId = creatorId;
            return this;
        }
    }
}
