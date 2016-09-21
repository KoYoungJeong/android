package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrChangeAccountName extends MainSprinklrModel {

    public SprinklrChangeAccountName() {
        super(SprinklerEvents.ChangeAccountName, true, false);
    }

    public static void sendSuccessLog() {
        new SprinklrChangeAccountName().sendSuccess();
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrChangeAccountName().sendFail(errorCode);
    }

}