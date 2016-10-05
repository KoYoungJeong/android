package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IEmail;

/**
 * Created by tee on 2016. 9. 8..
 */

public class SprinklrVerificationMail extends MainSprinklrModel
        implements IEmail {

    private SprinklrVerificationMail() {
        super(SprinklerEvents.SendAccountVerificationMail, false, false);
    }

    public static void sendLog(String email) {
        new SprinklrVerificationMail()
                .setEmail(email)
                .sendSuccess();
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrVerificationMail().sendFail(errorCode);
    }

    @Override
    public SprinklrVerificationMail setEmail(String email) {
        setProperty(PropertyKey.Email, email);
        return this;
    }

}
