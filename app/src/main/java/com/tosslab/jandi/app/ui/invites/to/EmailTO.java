package com.tosslab.jandi.app.ui.invites.to;

/**
 * Created by Steve SeongUg Jung on 14. 12. 27..
 */
public class EmailTO {
    private final String email;

    private EmailTO(String email) {
        this.email = email;
    }

    public static EmailTO create(String email) {
        return new EmailTO(email);
    }

    public String getEmail() {
        return email;
    }

}
