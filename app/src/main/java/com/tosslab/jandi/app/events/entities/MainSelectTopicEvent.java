package com.tosslab.jandi.app.events.entities;

/**
 * Created by Steve SeongUg Jung on 15. 7. 15..
 */
public class MainSelectTopicEvent {
    private final int selectedEntity;

    public MainSelectTopicEvent(int selectedEntity) {

        this.selectedEntity = selectedEntity;
    }

    public int getSelectedEntity() {
        return selectedEntity;
    }
}
