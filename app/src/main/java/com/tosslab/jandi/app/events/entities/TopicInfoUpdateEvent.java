package com.tosslab.jandi.app.events.entities;

/**
 * Created by Steve SeongUg Jung on 15. 4. 9..
 */
public class TopicInfoUpdateEvent {
    private final long id;

    public TopicInfoUpdateEvent(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
