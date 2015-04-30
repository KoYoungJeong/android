package com.tosslab.jandi.app.ui.invites.to;

/**
 * Created by Steve SeongUg Jung on 14. 12. 27..
 */
public class EmailTO {
    private final String email;
    private int isSuccess;

    private EmailTO(String email, int i) {
        this.email = email;
    }

    public static EmailTO create(String email) {
        return new EmailTO(email, 0);
    }

    public String getEmail() {
        return email;
    }

    public int getSuccess() {
        return isSuccess;
    }

    /**
     * 0 = progress<br/>
     * 1 = success<br/>
     * -1 = fail
     *
     * @param isSuccess
     */
    public void setSuccess(int isSuccess) {
        this.isSuccess = isSuccess;
    }
}
