package com.tosslab.jandi.app.events;

/**
 * Created by Steve SeongUg Jung on 15. 3. 11..
 */
public class SelectMemberEvent {
    private final long memberId;
    private final String name;

    public SelectMemberEvent(long memberId, String name) {

        this.memberId = memberId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public long getMemberId() {
        return memberId;
    }
}
