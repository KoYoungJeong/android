package com.tosslab.jandi.app.network.models;

/**
 * Created by tee on 2017. 4. 13..
 */

public class ReqChangePassword {

    private String oldPassword;
    private String newPassword;

    public ReqChangePassword(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
