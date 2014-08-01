package com.tosslab.jandi.app.network.models;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class ReqLogin {
    public String email;
    public String password;
    public int teamId;
    public ReqLogin(int teamId, String email, String passwd) {
        this.teamId = teamId;
        this.email = email;
        this.password = passwd;
    }
}
