package com.tosslab.jandi.app.events.messages;

/**
 * Created by Steve SeongUg Jung on 15. 2. 4..
 */
public class SendFailEvent {
    private final long localId;

    public SendFailEvent(long localId) {
        this.localId = localId;
    }

    public long getLocalId() {
        return localId;
    }
}
