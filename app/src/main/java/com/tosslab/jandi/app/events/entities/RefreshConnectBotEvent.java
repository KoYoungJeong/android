package com.tosslab.jandi.app.events.entities;

import com.tosslab.jandi.app.network.models.start.Bot;

public class RefreshConnectBotEvent {
    private final Bot bot;

    public RefreshConnectBotEvent(Bot bot) {
        this.bot = bot;
    }

    public Bot getBot() {
        return bot;
    }
}
