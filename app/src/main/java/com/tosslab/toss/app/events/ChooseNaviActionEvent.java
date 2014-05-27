package com.tosslab.toss.app.events;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class ChooseNaviActionEvent {
    public static int TYPE_CHENNEL  = 0;
    public static int TYPE_PRIVATE_GROUP    = 1;
    public static int TYPE_DIRECT_MESSAGE   = 2;

    public int type;    // 선택 경로의 타입
    public int id;      // 선택의 ID

    public ChooseNaviActionEvent(int type, int id) {
        this.type = type;
        this.id = id;
    }
}
