package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IEmail;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrChangeAccountPrimaryEmail extends MainSprinklrModel
        implements IEmail {

    private SprinklrChangeAccountPrimaryEmail() {
        super(SprinklerEvents.ChangeAccountPrimaryEmail, true, false);
    }

    public static void trackFail(int errorCode) {
        new SprinklrChangeAccountName().sendFail(errorCode);
    }

    public static void sendLog(String email) {
        new SprinklrChangeAccountPrimaryEmail()
                .setEmail(email)
                .sendSuccess();
    }

    @Override
    public MainSprinklrModel setEmail(String email) {
        setProperty(PropertyKey.Email, email);
        return this;
    }

}