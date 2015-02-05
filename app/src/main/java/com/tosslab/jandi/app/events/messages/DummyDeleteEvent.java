package com.tosslab.jandi.app.events.messages;

/**
 * Created by Steve SeongUg Jung on 15. 2. 5..
 */
public class DummyDeleteEvent {
    private final long localId;

    public DummyDeleteEvent(long localId) {
        this.localId = localId;
    }

    public long getLocalId() {
        return localId;
    }
}
