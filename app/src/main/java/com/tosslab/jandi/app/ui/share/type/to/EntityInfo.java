package com.tosslab.jandi.app.ui.share.type.to;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */
public class EntityInfo {
    private final int entityId;
    private final String name;
    private final boolean isPublicTopic;
    private final boolean isPrivateTopic;
    private final boolean user;
    private final String profileImage;

    public EntityInfo(int entityId, String name, boolean isPublicTopic, boolean isPrivateTopic, boolean user, String profileImage) {
        this.entityId = entityId;
        this.name = name;
        this.isPublicTopic = isPublicTopic;
        this.isPrivateTopic = isPrivateTopic;
        this.user = user;
        this.profileImage = profileImage;
    }

    public int getEntityId() {
        return entityId;
    }

    public String getName() {
        return name;
    }

    public boolean isPublicTopic() {
        return isPublicTopic;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public boolean isPrivateTopic() {
        return isPrivateTopic;
    }

    public boolean isUser() {
        return user;
    }
}
