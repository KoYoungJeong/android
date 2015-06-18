package com.tosslab.jandi.app.network.models;

/**
 * Created by Steve SeongUg Jung on 14. 12. 11..
 */
public class ReqAccountActivate {
    public String email;
    public String code;

    public ReqAccountActivate(String email, String code) {
        this.email = email;
        this.code = code;
    }

}
