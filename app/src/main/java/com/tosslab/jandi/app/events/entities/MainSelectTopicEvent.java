package com.tosslab.jandi.app.events.entities;

/**
 * Created by Steve SeongUg Jung on 15. 7. 15..
 */
public class MainSelectTopicEvent {
    private final long selectedEntity;

    public MainSelectTopicEvent(long selectedEntity) {

        this.selectedEntity = selectedEntity;
    }

    public long getSelectedEntity() {
        return selectedEntity;
    }
}
