package com.tosslab.jandi.app.events.entities;

public class TopicFolderRefreshEvent {
    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof TopicFolderRefreshEvent;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
