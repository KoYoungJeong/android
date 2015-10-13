package com.tosslab.jandi.app.network.models;

public class ReqMember {
    private final int memberId;

    public ReqMember(int memberId) {
        this.memberId = memberId;
    }

    public int getMemberId() {
        return memberId;
    }
}
