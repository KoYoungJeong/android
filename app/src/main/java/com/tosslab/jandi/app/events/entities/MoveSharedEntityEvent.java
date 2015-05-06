package com.tosslab.jandi.app.events.entities;

/**
 * Created by Steve SeongUg Jung on 15. 5. 5..
 */
public class MoveSharedEntityEvent {
    private final int entityId;

    public MoveSharedEntityEvent(int entityId) {
        this.entityId = entityId;
    }

    public int getEntityId() {
        return entityId;
    }
}
