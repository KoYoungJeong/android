package com.tosslab.jandi.app.events.entities;

import com.tosslab.jandi.app.network.models.start.Human;

public class ProfileChangeEvent {
    private final Human member;

    public ProfileChangeEvent(Human member) {

        this.member = member;
    }

    public Human getMember() {
        return member;
    }
}
