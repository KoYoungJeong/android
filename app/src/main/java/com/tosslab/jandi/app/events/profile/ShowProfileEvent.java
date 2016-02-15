package com.tosslab.jandi.app.events.profile;

/**
 * Created by justinygchoi on 2014. 9. 4..
 */
public class ShowProfileEvent {
    public long userId;
    public From from;

    public ShowProfileEvent(long userId) {
        this.userId = userId;
    }

    public ShowProfileEvent(long userId, From from) {
        this.userId = userId;
        this.from = from;
    }

    public enum From {
        Name, Image, Mention, SystemMessage
    }
}