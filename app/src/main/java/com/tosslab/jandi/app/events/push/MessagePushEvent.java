package com.tosslab.jandi.app.events.push;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
public class MessagePushEvent {
    private final int entityId;
    private final String entityType;

    public MessagePushEvent(int entityId, String entityType) {
        this.entityId = entityId;
        this.entityType = entityType;
    }

    public int getEntityId() {
        return entityId;
    }

    public String getEntityType() {
        return entityType;
    }
}
