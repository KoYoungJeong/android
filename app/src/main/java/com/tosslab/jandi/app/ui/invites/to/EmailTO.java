package com.tosslab.jandi.app.ui.invites.to;

/**
 * Created by Steve SeongUg Jung on 14. 12. 27..
 */
public class EmailTO {
    private final String email;
    private boolean isSuccess;

    private EmailTO(String email, boolean isSuccess) {
        this.email = email;
        this.isSuccess = isSuccess;
    }

    public static EmailTO create(String email, boolean success) {
        return new EmailTO(email, success);
    }

    public String getEmail() {
        return email;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
}
