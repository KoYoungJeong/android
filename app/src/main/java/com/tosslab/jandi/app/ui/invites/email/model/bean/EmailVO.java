package com.tosslab.jandi.app.ui.invites.email.model.bean;

/**
 * Created by Steve SeongUg Jung on 14. 12. 27..
 */
public class EmailVO {
    private final String email;
    private int isSuccess = 0;

    private EmailVO(String email) {
        this.email = email;
    }

    public static EmailVO create(String email) {
        return new EmailVO(email);
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
