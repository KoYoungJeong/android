package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 9. 4..
 */
public class RequestMoveDirectMessageEvent {
    public long userId;

    public RequestMoveDirectMessageEvent(long userId) {
        this.userId = userId;
    }
}
