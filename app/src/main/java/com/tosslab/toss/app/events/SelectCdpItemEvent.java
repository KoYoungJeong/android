package com.tosslab.toss.app.events;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class SelectCdpItemEvent {
    public String name; // 선택 CDP의 이름
    public int type;    // 선택 경로의 타입
    public int id;      // 선택의 ID

    public SelectCdpItemEvent(String name, int type, int id) {
        this.name = name;
        this.type = type;
        this.id = id;
    }
}
