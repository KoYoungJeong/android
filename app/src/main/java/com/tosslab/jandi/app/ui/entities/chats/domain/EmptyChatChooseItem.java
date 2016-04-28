package com.tosslab.jandi.app.ui.entities.chats.domain;

public class EmptyChatChooseItem extends ChatChooseItem {
    private final String query;

    public EmptyChatChooseItem(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
