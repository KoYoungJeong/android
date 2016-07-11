package com.tosslab.jandi.app.events.entities;

public class ChatListRefreshEvent {
    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof ChatListRefreshEvent;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
