package com.tosslab.jandi.app.events.profile;

import com.tosslab.jandi.app.network.models.ResAccountInfo;

/**
 * Created by Steve SeongUg Jung on 14. 12. 23..
 */
public class AccountEmailChangeEvent {
    private final ResAccountInfo.UserEmail userEmail;

    public AccountEmailChangeEvent(ResAccountInfo.UserEmail userEmail) {

        this.userEmail = userEmail;
    }

    public ResAccountInfo.UserEmail getUserEmail() {
        return userEmail;
    }
}
