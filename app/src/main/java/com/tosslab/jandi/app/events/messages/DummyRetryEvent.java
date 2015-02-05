package com.tosslab.jandi.app.events.messages;

/**
 * Created by Steve SeongUg Jung on 15. 2. 5..
 */
public class DummyRetryEvent {
    private final long localId;

    public DummyRetryEvent(long localId) {
        this.localId = localId;
    }

    public long getLocalId() {
        return localId;
    }
}
