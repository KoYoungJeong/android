package com.tosslab.jandi.app.network.models;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 7. 16..
 */
@Deprecated
public class ReqInviteUsers {
    public List<Long> inviteUsers;

    public ReqInviteUsers(List<Long> inviteUsers) {
        this.inviteUsers = inviteUsers;
    }

}
