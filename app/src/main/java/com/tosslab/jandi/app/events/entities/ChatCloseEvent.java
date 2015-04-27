package com.tosslab.jandi.app.events.entities;

/**
 * Created by Steve SeongUg Jung on 15. 4. 9..
 */
public class ChatCloseEvent {
    private final int companionId;

    public ChatCloseEvent(int companionId) {
        this.companionId = companionId;
    }

    public int getCompanionId() {
        return companionId;
    }
}
