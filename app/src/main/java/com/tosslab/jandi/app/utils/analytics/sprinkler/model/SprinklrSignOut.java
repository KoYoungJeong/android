package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrSignOut extends MainSprinklrModel {

    private SprinklrSignOut() {
        super(SprinklerEvents.SignOut, true, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrSignOut()
                .sendFailLog(errorCode);
    }

    public static void sendLog() {
        new SprinklrSignOut().sendSuccess();
    }

}