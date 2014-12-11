package com.tosslab.jandi.app.network.models;

/**
 * Created by Steve SeongUg Jung on 14. 12. 11..
 */
public class ReqAccountActivate {
    private String email;
    private String token;

    public ReqAccountActivate(String email, String token) {
        this.email = email;
        this.token = token;
    }
}
