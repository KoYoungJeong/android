package com.tosslab.jandi.app.events.profile;

/**
 * Created by justinygchoi on 2014. 9. 4..
 */
public class ShowProfileEvent {
    public int userId;
    public From from;

    public ShowProfileEvent(int userId) {
        this.userId = userId;
    }

    public ShowProfileEvent(int userId, From from) {
        this.userId = userId;
        this.from = from;
    }

    public enum From {
        Name, Image, Mention, SystemMessage
    }
}