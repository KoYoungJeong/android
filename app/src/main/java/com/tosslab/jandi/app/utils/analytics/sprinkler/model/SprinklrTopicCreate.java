package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.ITopicId;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrTopicCreate extends MainSprinklrModel
        implements ITopicId {

    private SprinklrTopicCreate() {
        super(SprinklerEvents.TopicCreate, true, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrTopicCreate().sendFail(errorCode);
    }

    public static void sendLog(long topicId) {
        new SprinklrTopicCreate()
                .setTopicId(topicId)
                .sendSuccess();
    }

    @Override
    public MainSprinklrModel setTopicId(long topicId) {
        setProperty(PropertyKey.TopicId, topicId);
        return this;
    }

}
