package com.tosslab.jandi.app.events.profile;

/**
 * Created by Steve SeongUg Jung on 15. 1. 14..
 */
public class ProfileDetailEvent {
    private final int entityId;

    public ProfileDetailEvent(int entityId) {
        this.entityId = entityId;
    }

    public int getEntityId() {
        return entityId;
    }
}
