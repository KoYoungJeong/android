package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IFileId;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IHasAllMention;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IMentionCount;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IMessageId;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IPollId;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IStickerId;

/**
 * Created by tee on 2016. 9. 8..
 */

public class SprinklrMessagePost extends MainSprinklrModel
        implements IMessageId, IMentionCount, IHasAllMention,
        IFileId, IStickerId, IPollId {

    private SprinklrMessagePost() {
        super(SprinklerEvents.MessagePost, true, true);
    }

    public static void sendLogWithMessage(long messageId,
                                          int mentionCount,
                                          boolean hasAllMention) {
        new SprinklrMessagePost()
                .setMessageId(messageId)
                .setMentionCount(mentionCount)
                .setHasAllMention(hasAllMention)
                .sendSuccess();
    }

    public static void sendLogWithFileComment(long commentId,
                                              long fileId,
                                              int mentionCount,
                                              boolean hasAllMention) {
        new SprinklrMessagePost()
                .setMessageId(commentId)
                .setMentionCount(mentionCount)
                .setFileId(fileId)
                .setHasAllMention(hasAllMention)
                .sendSuccess();
    }

    public static void sendLogWithPollComment(long commentId,
                                              long pollId,
                                              int mentionCount,
                                              boolean hasAllMention) {
        new SprinklrMessagePost()
                .setMessageId(commentId)
                .setPollId(pollId)
                .setMentionCount(mentionCount)
                .setHasAllMention(hasAllMention)
                .sendSuccess();
    }

    public static void sendLogWithSticker(long messageId,
                                          String stickerId) {
        new SprinklrMessagePost()
                .setMessageId(messageId)
                .setMentionCount(0)
                .setHasAllMention(false)
                .setStickerId(stickerId)
                .sendSuccess();
    }

    public static void sendLogWithStickerFile(long messageId,
                                              String stickerId,
                                              long fileId,
                                              int mentionCount,
                                              boolean hasAllMention) {
        new SprinklrMessagePost()
                .setMessageId(messageId)
                .setMentionCount(mentionCount)
                .setHasAllMention(hasAllMention)
                .setStickerId(stickerId)
                .setFileId(fileId)
                .sendSuccess();
    }

    public static void sendLogWithStickerPoll(long messageId,
                                              String stickerId,
                                              long pollId,
                                              int mentionCount,
                                              boolean hasAllMention) {
        new SprinklrMessagePost()
                .setMessageId(messageId)
                .setMentionCount(mentionCount)
                .setHasAllMention(hasAllMention)
                .setStickerId(stickerId)
                .setPollId(pollId)
                .sendSuccess();
    }

    public static void trackFail(int errorCode) {
        new SprinklrMessagePost().sendFail(errorCode);
    }

    @Override
    public SprinklrMessagePost setHasAllMention(boolean hasAllMention) {
        setProperty(PropertyKey.HasAllMention, hasAllMention);
        return this;
    }

    @Override
    public SprinklrMessagePost setMentionCount(int count) {
        setProperty(PropertyKey.MentionCount, count);
        return this;
    }

    @Override
    public SprinklrMessagePost setMessageId(long messageId) {
        setProperty(PropertyKey.MessageId, messageId);
        return this;
    }

    @Override
    public SprinklrMessagePost setFileId(long fileId) {
        setProperty(PropertyKey.FileId, fileId);
        return this;
    }

    @Override
    public SprinklrMessagePost setStickerId(String stickerId) {
        setProperty(PropertyKey.StickerId, stickerId);
        return this;
    }

    @Override
    public SprinklrMessagePost setPollId(long pollId) {
        setProperty(PropertyKey.PollId, pollId);
        return this;
    }

}
