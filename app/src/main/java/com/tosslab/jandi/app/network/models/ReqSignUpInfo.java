package com.tosslab.jandi.app.network.models;

/**
 * Created by Steve SeongUg Jung on 14. 12. 11..
 */
public class ReqSignUpInfo {

    public String email;
    public String password;
    public String name;
    public String lang;

    public ReqSignUpInfo(String email, String password, String name, String lang) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.lang = lang;
    }
}
