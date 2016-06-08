package com.tosslab.jandi.app.events.entities;

public class TopicInfoUpdateEvent {
    private final long id;

    public TopicInfoUpdateEvent(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
