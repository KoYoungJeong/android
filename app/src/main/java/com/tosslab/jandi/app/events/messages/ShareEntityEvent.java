package com.tosslab.jandi.app.events.messages;

/**
 * Created by Steve SeongUg Jung on 15. 2. 26..
 */
public class ShareEntityEvent {
    private final long entityId;
    private final int entityType;
    private final String text;

    public ShareEntityEvent(long entityId, int entityType, String text) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.text = text;
    }

    public long getEntityId() {
        return entityId;
    }

    public String getText() {
        return text;
    }

    public int getEntityType() {
        return entityType;
    }
}
