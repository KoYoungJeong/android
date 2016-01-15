package com.tosslab.jandi.app.events.entities;

import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

public class RefreshConnectBotEvent {
    private final ResLeftSideMenu.Bot bot;

    public RefreshConnectBotEvent(ResLeftSideMenu.Bot bot) {
        this.bot = bot;
    }

    public ResLeftSideMenu.Bot getBot() {
        return bot;
    }
}
