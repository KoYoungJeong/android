package com.tosslab.jandi.app.network.models;

public class ReqMember {
    private final long memberId;

    public ReqMember(long memberId) {
        this.memberId = memberId;
    }

    public long getMemberId() {
        return memberId;
    }
}
