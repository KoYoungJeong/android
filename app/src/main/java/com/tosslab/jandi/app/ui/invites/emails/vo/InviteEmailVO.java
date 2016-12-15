package com.tosslab.jandi.app.ui.invites.emails.vo;

/**
 * Created by tee on 2016. 12. 9..
 */

public class InviteEmailVO {

    private String email;
    private Status status;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        AVAILABLE, JOINED, DUMMY, BLOCKED, ACCOUNT_JOIN, ACCOUNT_DUMMY, ACCOUNT_BLOCKED
    }
}
