package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 9. 4..
 */
public class RequestUserInfoEvent {
    public int userId;
    public From from;

    public RequestUserInfoEvent(int userId, From from) {
        this.userId = userId;
        this.from = from;
    }

    public RequestUserInfoEvent(int userId) {
        this.userId = userId;

    }

    public enum From {
        Name, Image, Mention, SystemMessage
    }
}
