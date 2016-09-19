package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IFileId;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IMessageId;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IPollId;

/**
 * Created by tee on 2016. 9. 8..
 */

public class SprinklrStarred extends MainSprinklrModel
        implements IMessageId, IFileId, IPollId {

    private SprinklrStarred() {
        super(SprinklerEvents.Starred, true, true);
    }

    public static void sendLogWithMessageId(long messageId) {
        new SprinklrStarred()
                .setMessageId(messageId)
                .sendSuccess();
    }

    public static void sendLogWithCommentId(long commentId) {
        new SprinklrStarred()
                .setMessageId(commentId)
                .sendSuccess();
    }

    public static void sendLogWithFileId(long fileId) {
        new SprinklrStarred()
                .setFileId(fileId)
                .sendSuccess();
    }

    public static void sendLogWithPollId(long pollId) {
        new SprinklrStarred()
                .setPollId(pollId)
                .sendSuccess();
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrStarred().sendFail(errorCode);
    }

    @Override
    public SprinklrStarred setFileId(long fileId) {
        setProperty(PropertyKey.FileId, fileId);
        setProperty(PropertyKey.StarredType, "file");
        return this;
    }

    @Override
    public SprinklrStarred setPollId(long pollId) {
        setProperty(PropertyKey.PollId, pollId);
        setProperty(PropertyKey.StarredType, "poll");
        return this;
    }

    @Override
    public SprinklrStarred setMessageId(long messageId) {
        setProperty(PropertyKey.MessageId, messageId);
        setProperty(PropertyKey.StarredType, "message");
        return this;
    }

}
