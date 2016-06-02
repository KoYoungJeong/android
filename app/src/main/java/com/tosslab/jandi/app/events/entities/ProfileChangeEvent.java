package com.tosslab.jandi.app.events.entities;

import com.tosslab.jandi.app.team.member.User;

public class ProfileChangeEvent {
    private final User member;

    public ProfileChangeEvent(User member) {

        this.member = member;
    }

    public User getMember() {
        return member;
    }
}
