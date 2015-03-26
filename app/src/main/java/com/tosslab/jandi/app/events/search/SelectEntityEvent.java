package com.tosslab.jandi.app.events.search;

/**
 * Created by Steve SeongUg Jung on 15. 3. 11..
 */
public class SelectEntityEvent {
    private final int entityId;
    private final String name;

    public SelectEntityEvent(int entityId, String name) {

        this.entityId = entityId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getEntityId() {
        return entityId;
    }
}
