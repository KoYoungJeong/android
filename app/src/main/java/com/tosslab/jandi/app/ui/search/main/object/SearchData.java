package com.tosslab.jandi.app.ui.search.main.object;

/**
 * Created by tee on 16. 7. 21..
 */
public class SearchData {

    public static final int ITEM_TYPE_MESSAGE_HEADER = 0x01;
    public static final int ITEM_TYPE_MESSAGE_ITEM = 0x02;
    public static final int ITEM_TYPE_NO_MESSAGE_ITEM = 0x03;
    public static final int ITEM_TYPE_ROOM_HEADER = 0x04;
    public static final int ITEM_TYPE_ROOM_ITEM = 0x05;
    public static final int ITEM_TYPE_NO_ROOM_ITEM = 0x06;
    public static final int ITEM_TYPE_HISTORY_HEADER = 0x07;
    public static final int ITEM_TYPE_HISTORY_ITEM = 0x08;
    public static final int ITEM_TYPE_NO_HISTORY_ITEM = 0x09;
    public static final int ITEM_TYPE_MESSAGE_HEADER_FOR_HISTORY = 0x10;

    protected int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
