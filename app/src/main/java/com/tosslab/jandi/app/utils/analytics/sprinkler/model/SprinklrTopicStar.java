package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.ITopicId;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrTopicStar extends MainSprinklrModel
        implements ITopicId {

    private SprinklrTopicStar() {
        super(SprinklerEvents.TopicStar, true, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrTopicStar().sendFail(errorCode);
    }

    public static void sendLog(long topicId) {
        new SprinklrTopicStar()
                .setTopicId(topicId)
                .sendSuccess();
    }

    @Override
    public SprinklrTopicStar setTopicId(long topicId) {
        setProperty(PropertyKey.TopicId, topicId);
        return this;
    }

}
