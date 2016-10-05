package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IEmail;

/**
 * Created by tee on 2016. 9. 12..
 */

public class SprinklrResendVerificationEmail extends MainSprinklrModel
        implements IEmail {

    private SprinklrResendVerificationEmail() {
        super(SprinklerEvents.ResendAccountVerificationMail, true, false);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrResendVerificationEmail()
                .sendFail(errorCode);
    }

    public static void sendLog(String email) {
        new SprinklrResendVerificationEmail()
                .setEmail(email)
                .sendSuccess();
    }

    @Override
    public MainSprinklrModel setEmail(String email) {
        setProperty(PropertyKey.Email, email);
        return this;
    }

}
