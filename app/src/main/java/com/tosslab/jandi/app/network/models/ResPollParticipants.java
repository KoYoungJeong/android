package com.tosslab.jandi.app.network.models;

import java.util.List;

/**
 * Created by tonyjs on 16. 6. 27..
 */
public class ResPollParticipants {
    private List<Long> memberIds;

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }
}
