package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 9. 4..
 */
public class RequestMoveDirectMessageEvent {
    public int userId;
    public String userName;
    public RequestMoveDirectMessageEvent(int userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }
}
