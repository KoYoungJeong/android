package com.tosslab.jandi.app.network.models;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 7. 16..
 */
public class ReqInviteTopicUsers extends ReqInviteUsers{

    public int teamId;

    public ReqInviteTopicUsers(List<Integer> inviteUsers, int teamId) {
        super(inviteUsers);
        this.teamId = teamId;
    }

}
