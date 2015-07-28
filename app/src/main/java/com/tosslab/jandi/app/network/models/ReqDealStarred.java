package com.tosslab.jandi.app.network.models;

/**
 * Created by tee on 15. 7. 27..
 */
public class ReqDealStarred {

    private int roomId;

    public void ReqRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getRoomId() {
        return roomId;
    }
}
