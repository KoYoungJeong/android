package com.tosslab.jandi.app.events.entities;

/**
 * Created by Steve SeongUg Jung on 15. 5. 5..
 */
public class MoveSharedEntityEvent {
    private final long entityId;

    public MoveSharedEntityEvent(long entityId) {
        this.entityId = entityId;
    }

    public long getEntityId() {
        return entityId;
    }
}
