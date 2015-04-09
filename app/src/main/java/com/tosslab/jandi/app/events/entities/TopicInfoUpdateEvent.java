package com.tosslab.jandi.app.events.entities;

/**
 * Created by Steve SeongUg Jung on 15. 4. 9..
 */
public class TopicInfoUpdateEvent {
    private final int id;

    public TopicInfoUpdateEvent(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
