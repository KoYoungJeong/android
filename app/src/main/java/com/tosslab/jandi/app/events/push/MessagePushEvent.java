package com.tosslab.jandi.app.events.push;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
public class MessagePushEvent {
    private final long entityId;
    private final String entityType;

    public MessagePushEvent(long entityId, String entityType) {
        this.entityId = entityId;
        this.entityType = entityType;
    }

    public long getEntityId() {
        return entityId;
    }

    public String getEntityType() {
        return entityType;
    }
}
