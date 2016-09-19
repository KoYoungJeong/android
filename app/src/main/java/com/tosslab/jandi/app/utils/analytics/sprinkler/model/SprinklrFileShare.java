package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IFileId;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.ITopicId;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrFileShare extends MainSprinklrModel
        implements ITopicId, IFileId {

    private SprinklrFileShare() {
        super(SprinklerEvents.FileShare, true, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrFileShare().sendFail(errorCode);
    }

    public static void sendLog(long topicId, long fileId) {
        new SprinklrFileShare()
                .setFileId(fileId)
                .setTopicId(topicId)
                .sendSuccess();
    }

    @Override
    public SprinklrFileShare setFileId(long fileId) {
        setProperty(PropertyKey.FileId, fileId);
        return this;
    }

    @Override
    public SprinklrFileShare setTopicId(long topicId) {
        setProperty(PropertyKey.TopicId, topicId);
        return this;
    }

}
