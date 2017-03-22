package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IPollId;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IPropertyMemberId;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.ITeamId;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.ITopicId;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrPollVoted extends MainSprinklrModel
        implements IPropertyMemberId, IPollId, ITeamId, ITopicId {

    private SprinklrPollVoted() {
        super(SprinklerEvents.PollVoted, true, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrPollVoted().sendFail(errorCode);
    }

    public static void sendLog(long memberId, long pollId, long teamId, long topicId) {
        new SprinklrPollVoted()
                .setPropertyMemberId(memberId)
                .setPollId(pollId)
                .setTeamId(teamId)
                .setTopicId(topicId)
                .sendSuccess();
    }

    @Override
    public SprinklrPollVoted setPropertyMemberId(long memberId) {
        setProperty(PropertyKey.MemberId, memberId);
        return this;
    }

    @Override
    public SprinklrPollVoted setPollId(long pollId) {
        setProperty(PropertyKey.PollId, pollId);
        return this;
    }

    @Override
    public SprinklrPollVoted setTeamId(long teamId) {
        setProperty(PropertyKey.TeamId, teamId);
        return this;
    }

    @Override
    public SprinklrPollVoted setTopicId(long topicId) {
        setProperty(PropertyKey.TopicId, topicId);
        return this;
    }

}
