package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 7. 12..
 */
public class RequestMessageListEvent {
    public int type;    // 선택 경로의 타입
    public int id;      // 선택의 ID

    public RequestMessageListEvent(int type, int id) {
        this.type = type;
        this.id = id;
    }
}
