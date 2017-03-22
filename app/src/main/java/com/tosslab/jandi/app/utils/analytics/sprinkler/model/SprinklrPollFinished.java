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

public class SprinklrPollFinished extends MainSprinklrModel
        implements IPropertyMemberId, IPollId, ITeamId, ITopicId {

    private SprinklrPollFinished() {
        super(SprinklerEvents.PollFinished, true, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrPollFinished().sendFail(errorCode);
    }

    public static void sendLog(long memberId, long pollId, long teamId, long topicId) {
        new SprinklrPollFinished()
                .setPropertyMemberId(memberId)
                .setPollId(pollId)
                .setTeamId(teamId)
                .setTopicId(topicId)
                .sendSuccess();
    }

    @Override
    public SprinklrPollFinished setPropertyMemberId(long memberId) {
        setProperty(PropertyKey.MemberId, memberId);
        return this;
    }

    @Override
    public SprinklrPollFinished setPollId(long pollId) {
        setProperty(PropertyKey.PollId, pollId);
        return this;
    }

    @Override
    public SprinklrPollFinished setTeamId(long teamId) {
        setProperty(PropertyKey.TeamId, teamId);
        return this;
    }

    @Override
    public SprinklrPollFinished setTopicId(long topicId) {
        setProperty(PropertyKey.TopicId, topicId);
        return this;
    }

}
