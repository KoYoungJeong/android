package com.tosslab.jandi.app.events.team;

/**
 * Created by tee on 2017. 5. 31..
 */

public class MemberAbsenceInfoChangeEvent {
    private long memberId;

    public MemberAbsenceInfoChangeEvent(long memberId) {
        this.memberId = memberId;
    }

    public long getMemberId() {
        return memberId;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }
}
