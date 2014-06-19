package com.tosslab.toss.app.events;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class SelectCdpItemEvent {
    public int type;    // 선택 경로의 타입
    public int id;      // 선택의 ID

    public SelectCdpItemEvent(int type, int id) {
        this.type = type;
        this.id = id;
    }
}
