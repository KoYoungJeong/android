package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 9. 4..
 */
public class RequestMoveDirectMessageEvent {
    public int userId;

    public RequestMoveDirectMessageEvent(int userId) {
        this.userId = userId;
    }
}
