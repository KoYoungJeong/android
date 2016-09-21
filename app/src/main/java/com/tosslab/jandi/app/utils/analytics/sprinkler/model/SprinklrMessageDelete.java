package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IMessageId;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrMessageDelete extends MainSprinklrModel
        implements IMessageId {

    public SprinklrMessageDelete() {
        super(SprinklerEvents.MessageDelete, true, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrMessageDelete().sendFail(errorCode);
    }

    public static void sendLog(long messageId) {
        new SprinklrMessageDelete().setMessageId(messageId).sendSuccess();
    }

    @Override
    public MainSprinklrModel setMessageId(long messageId) {
        setProperty(PropertyKey.MessageId, messageId);
        return this;
    }

}
