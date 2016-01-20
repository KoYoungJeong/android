package com.tosslab.jandi.app.events.messages;

/**
 * Created by Steve SeongUg Jung on 15. 2. 4..
 */
public class SendCompleteEvent {
    private final long localId;
    private final long id;

    public SendCompleteEvent(long localId, long id) {
        this.localId = localId;
        this.id = id;
    }

    public long getLocalId() {
        return localId;
    }

    public long getId() {
        return id;
    }
}
