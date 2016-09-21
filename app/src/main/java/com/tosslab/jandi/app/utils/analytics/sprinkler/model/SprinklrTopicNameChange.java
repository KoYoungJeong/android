package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.ITopicId;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrTopicNameChange extends MainSprinklrModel
        implements ITopicId {

    private SprinklrTopicNameChange() {
        super(SprinklerEvents.TopicNameChange, true, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrTopicNameChange().sendFail(errorCode);
    }

    public static void sendLog(long topicId) {
        new SprinklrTopicNameChange()
                .setTopicId(topicId)
                .sendSuccess();
    }

    @Override
    public SprinklrTopicNameChange setTopicId(long topicId) {
        setProperty(PropertyKey.TopicId, topicId);
        return this;
    }

}
