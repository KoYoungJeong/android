package com.tosslab.toss.app.navigation;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class CdpItem {
    public static final int TYPE_CHANNEL = 0;
    public static final int TYPE_DIRECT_MESSAGE = 1;
    public static final int TYPE_PRIVATE_GROUP = 2;

    public final String name;
    public final String userId;
    public final int type;
    public final int id;


    public CdpItem(String nickname, String userId) {
        this.name = nickname;
        this.type = TYPE_DIRECT_MESSAGE;
        this.userId = userId;
        this.id = -1;
    }

    public CdpItem(String name, int type, int id) {
        this.name = name;
        this.type = type;
        this.userId = null;
        this.id = id;
    }
}
