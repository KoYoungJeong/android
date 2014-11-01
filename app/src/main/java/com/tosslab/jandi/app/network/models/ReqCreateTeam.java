package com.tosslab.jandi.app.network.models;

/**
 * Created by justinygchoi on 2014. 10. 13..
 */
public class ReqCreateTeam {
    public String email;
    public String lang;
    public ReqCreateTeam(String email, String lang) {
        this.email = email;
        this.lang = lang;
    }
}
