package com.tosslab.jandi.app.events.entities;

/**
 * Created by Steve SeongUg Jung on 15. 4. 9..
 */
public class ProfileChangeEvent {
    private final int entityId;

    public ProfileChangeEvent(int id) {

        entityId = id;
    }

    public int getEntityId() {
        return entityId;
    }
}
