package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 5. 27..
 * MainLeftFragment -> MainMessageListFragment : Navigation Panel 에서 선택한 CDP 의 메시지 list 획득 목적
 * MainLeftFragment -> MainActivity : 네비게이션 드로어를 닫아줘야 하기 때문에 후킹
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
