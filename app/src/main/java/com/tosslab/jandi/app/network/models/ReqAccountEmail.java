package com.tosslab.jandi.app.network.models;

/**
 * Created by Steve SeongUg Jung on 14. 12. 15..
 */
public class ReqAccountEmail {
    private String email;
    private String lang;

    public ReqAccountEmail(String email) {
        this.email = email;
    }

    public ReqAccountEmail(String email, String lang) {
        this.email = email;
        this.lang = lang;
    }

    public String getEmail() {
        return email;
    }

    public String getLang() {
        return lang;
    }
}
