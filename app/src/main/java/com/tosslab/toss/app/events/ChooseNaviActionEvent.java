package com.tosslab.toss.app.events;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class ChooseNaviActionEvent {
    public static final int TYPE_CHENNEL  = 0;
    public static final int TYPE_DIRECT_MESSAGE   = 1;
    public static final int TYPE_PRIVATE_GROUP    = 2;


    public int type;    // 선택 경로의 타입
    public int id;      // 선택의 ID
    public String userId;   // Direct message 인 경우 사용자 Id

    public ChooseNaviActionEvent(int type, String userId) {
        this.type = type;
        this.userId = userId;
        this.id = -1;
    }
    public ChooseNaviActionEvent(int type, int id) {
        this.type = type;
        this.id = id;
        this.userId = null;
    }
}
