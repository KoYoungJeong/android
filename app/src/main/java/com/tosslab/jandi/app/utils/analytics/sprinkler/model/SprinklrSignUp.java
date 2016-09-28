package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;

/**
 * Created by tee on 2016. 9. 12..
 */

public class SprinklrSignUp extends MainSprinklrModel {

    private SprinklrSignUp() {
        super(SprinklerEvents.SignUp, true, false);
    }

    public static void trackFail(int errorCode) {
        new SprinklrSignUp().sendFail(errorCode);
    }

    public static void sendLog() {
        new SprinklrSignUp().sendSuccess();
    }

}