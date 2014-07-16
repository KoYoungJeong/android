package com.tosslab.jandi.app.network.models;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 7. 16..
 */
public class ReqInviteUsers {
    public List<Integer> inviteUsers;
    public ReqInviteUsers(List<Integer> inviteUsers) {
        this.inviteUsers = inviteUsers;
    }

}
