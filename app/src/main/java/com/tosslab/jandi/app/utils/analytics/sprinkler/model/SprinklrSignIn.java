package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IAutoSignIn;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.ILoginId;

/**
 * Created by tee on 2016. 9. 8..
 */

public class SprinklrSignIn extends MainSprinklrModel
        implements IAutoSignIn, ILoginId {

    private SprinklrSignIn(boolean hasTeamSelected) {
        super(SprinklerEvents.SignIn, true, hasTeamSelected);
    }

    private SprinklrSignIn() {
        super(SprinklerEvents.SignIn, true, false);
    }

    public static void sendLog(boolean hasTeamSelected, boolean autoSignIn, String email) {
        new SprinklrSignIn(hasTeamSelected)
                .setAutoSignIn(autoSignIn)
                .setLoginId(email)
                .sendSuccess();
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrSignIn().sendFail(errorCode);
    }

    @Override
    public SprinklrSignIn setAutoSignIn(boolean isAutoSignIn) {
        setProperty(PropertyKey.AutoSignIn, isAutoSignIn);
        return this;
    }

    @Override
    public SprinklrSignIn setLoginId(String loginId) {
        setProperty(PropertyKey.LoginId, loginId);
        return this;
    }
}
