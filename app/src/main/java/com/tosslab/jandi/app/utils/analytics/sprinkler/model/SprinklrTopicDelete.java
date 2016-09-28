package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.ITopicId;

/**
 * Created by tee on 2016. 9. 19..
 */

public class SprinklrTopicDelete extends MainSprinklrModel
        implements ITopicId {

    private SprinklrTopicDelete() {
        super(SprinklerEvents.TopicDelete, true, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrTopicDelete().sendFail(errorCode);
    }

    public static void sendLog(long topicId) {
        new SprinklrTopicDelete()
                .setTopicId(topicId)
                .sendSuccess();
    }

    @Override
    public MainSprinklrModel setTopicId(long topicId) {
        setProperty(PropertyKey.TopicId, topicId);
        return this;
    }

}
