package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IEmail;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrRequestVerificationEmail extends MainSprinklrModel
        implements IEmail {

    private SprinklrRequestVerificationEmail() {
        super(SprinklerEvents.RequestVerificationEmail, true, false);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrRequestVerificationEmail()
                .sendFail(errorCode);
    }

    public static void sendLog(String email) {
        new SprinklrRequestVerificationEmail()
                .setEmail(email)
                .sendSuccess();
    }

    @Override
    public MainSprinklrModel setEmail(String email) {
        setProperty(PropertyKey.Email, email);
        return this;
    }
}
