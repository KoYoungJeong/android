package com.tosslab.jandi.app.events.messages;

/**
 * Created by Steve SeongUg Jung on 15. 2. 26..
 */
public class ShareEntityEvent {
    private final int entityId;
    private final int entityType;
    private final String text;

    public ShareEntityEvent(int entityId, int entityType, String text) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.text = text;
    }

    public int getEntityId() {
        return entityId;
    }

    public String getText() {
        return text;
    }

    public int getEntityType() {
        return entityType;
    }
}
