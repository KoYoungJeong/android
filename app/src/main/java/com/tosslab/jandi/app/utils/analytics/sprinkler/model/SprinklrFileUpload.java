package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IFileId;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.ITopicId;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrFileUpload extends MainSprinklrModel
        implements ITopicId, IFileId {

    private SprinklrFileUpload() {
        super(SprinklerEvents.FileUpload, true, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrFileUpload().sendFail(errorCode);
    }

    public static void sendLog(long topicId, long fileId) {
        new SprinklrFileUpload()
                .setFileId(fileId)
                .setTopicId(topicId)
                .sendSuccess();
    }

    @Override
    public SprinklrFileUpload setFileId(long fileId) {
        setProperty(PropertyKey.FileId, fileId);
        return this;
    }

    @Override
    public SprinklrFileUpload setTopicId(long topicId) {
        setProperty(PropertyKey.TopicId, topicId);
        return this;
    }

}
