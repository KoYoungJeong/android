package com.tosslab.jandi.app.network.models;

/**
 * Created by tee on 2017. 6. 7..
 */

public class ReqGooroomeOtp {

    private String roomId;
    private String userName;
    private String roleId;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }
}
