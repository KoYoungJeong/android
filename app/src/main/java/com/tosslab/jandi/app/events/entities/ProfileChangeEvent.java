package com.tosslab.jandi.app.events.entities;

import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

/**
 * Created by Steve SeongUg Jung on 15. 4. 9..
 */
public class ProfileChangeEvent {
    private final ResLeftSideMenu.User member;

    public ProfileChangeEvent(ResLeftSideMenu.User member) {

        this.member = member;
    }

    public ResLeftSideMenu.User getMember() {
        return member;
    }
}
