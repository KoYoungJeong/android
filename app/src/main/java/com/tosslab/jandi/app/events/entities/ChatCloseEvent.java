package com.tosslab.jandi.app.events.entities;

/**
 * Created by Steve SeongUg Jung on 15. 4. 9..
 */
public class ChatCloseEvent {
    private final long companionId;

    public ChatCloseEvent(long companionId) {
        this.companionId = companionId;
    }

    public long getCompanionId() {
        return companionId;
    }
}
