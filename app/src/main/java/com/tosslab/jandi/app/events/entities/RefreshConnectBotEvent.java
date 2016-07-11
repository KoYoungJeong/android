package com.tosslab.jandi.app.events.entities;

public class RefreshConnectBotEvent {

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof RefreshConnectBotEvent;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
