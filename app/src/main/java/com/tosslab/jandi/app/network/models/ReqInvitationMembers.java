package com.tosslab.jandi.app.network.models;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 11..
 */
public class ReqInvitationMembers {
    public long teamId;
    public List<String> receivers;
    public String lang;

    public ReqInvitationMembers(long teamId, List<String> receivers, String lang) {
        this.teamId = teamId;
        this.receivers = receivers;
        this.lang = lang;
    }
}
