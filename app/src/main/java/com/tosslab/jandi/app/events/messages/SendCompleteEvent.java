package com.tosslab.jandi.app.events.messages;

/**
 * Created by Steve SeongUg Jung on 15. 2. 4..
 */
public class SendCompleteEvent {
    private final long localId;
    private final int id;

    public SendCompleteEvent(long localId, int id) {
        this.localId = localId;
        this.id = id;
    }

    public long getLocalId() {
        return localId;
    }

    public int getId() {
        return id;
    }
}
