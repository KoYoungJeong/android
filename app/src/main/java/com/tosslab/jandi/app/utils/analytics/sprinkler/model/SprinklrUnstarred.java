package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IFileId;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IMessageId;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IPollId;

/**
 * Created by tee on 2016. 9. 8..
 */

public class SprinklrUnstarred extends MainSprinklrModel
        implements IMessageId, IFileId, IPollId {

    private SprinklrUnstarred() {
        super(SprinklerEvents.UnStarred, true, true);
    }

    public static void sendLogWithMessageId(long messageId) {
        new SprinklrUnstarred()
                .setMessageId(messageId)
                .sendSuccess();
    }

    public static void sendLogWithCommentId(long commentId) {
        new SprinklrUnstarred()
                .setMessageId(commentId)
                .sendSuccess();
    }

    public static void sendLogWithFileId(long fileId) {
        new SprinklrUnstarred()
                .setFileId(fileId)
                .sendSuccess();
    }

    public static void sendLogWithPollId(long pollId) {
        new SprinklrUnstarred()
                .setPollId(pollId)
                .sendSuccess();
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrUnstarred().sendFail(errorCode);
    }

    @Override
    public SprinklrUnstarred setFileId(long fileId) {
        setProperty(PropertyKey.FileId, fileId);
        setProperty(PropertyKey.StarredType, "file");
        return this;
    }

    @Override
    public SprinklrUnstarred setPollId(long pollId) {
        setProperty(PropertyKey.PollId, pollId);
        setProperty(PropertyKey.StarredType, "poll");
        return this;
    }

    @Override
    public SprinklrUnstarred setMessageId(long messageId) {
        setProperty(PropertyKey.MessageId, messageId);
        setProperty(PropertyKey.StarredType, "message");
        return this;
    }

}
