package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.ITopicId;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrTopicUnStar extends MainSprinklrModel
        implements ITopicId {

    private SprinklrTopicUnStar() {
        super(SprinklerEvents.TopicUnStar, true, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrTopicUnStar().sendFail(errorCode);
    }

    public static void sendLog(long topicId) {
        new SprinklrTopicUnStar()
                .setTopicId(topicId)
                .sendSuccess();
    }

    @Override
    public SprinklrTopicUnStar setTopicId(long topicId) {
        setProperty(PropertyKey.TopicId, topicId);
        return this;
    }

}
