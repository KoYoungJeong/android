package com.tosslab.jandi.app.events.entities;

public class RetrieveTopicListEvent {

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof RetrieveTopicListEvent;

    }

    @Override
    public int hashCode() {
        return 0;
    }
}
