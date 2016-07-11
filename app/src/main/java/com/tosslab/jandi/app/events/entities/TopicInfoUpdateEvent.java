package com.tosslab.jandi.app.events.entities;

public class TopicInfoUpdateEvent {
    private final long id;

    public TopicInfoUpdateEvent(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TopicInfoUpdateEvent that = (TopicInfoUpdateEvent) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
