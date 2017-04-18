package com.tosslab.jandi.app.events.team;

/**
 * Created by tee on 2017. 4. 17..
 */

public class MemberOnlineStatusChangeEvent {

    private long memberId;
    private String presence;

    public MemberOnlineStatusChangeEvent(long memberId, String presence) {
        this.memberId = memberId;
        this.presence = presence;
    }

    public long getMemberId() {
        return memberId;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }

    public String getPresence() {
        return presence;
    }

    public void setPresence(String presence) {
        this.presence = presence;
    }
}
