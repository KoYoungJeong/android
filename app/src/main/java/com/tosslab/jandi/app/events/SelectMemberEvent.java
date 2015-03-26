package com.tosslab.jandi.app.events;

/**
 * Created by Steve SeongUg Jung on 15. 3. 11..
 */
public class SelectMemberEvent {
    private final int memberId;
    private final String name;

    public SelectMemberEvent(int memberId, String name) {

        this.memberId = memberId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getMemberId() {
        return memberId;
    }
}
