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

public class SprinklrPollDeleted extends MainSprinklrModel
        implements IPropertyMemberId, IPollId, ITeamId, ITopicId {

    private SprinklrPollDeleted() {
        super(SprinklerEvents.PollDeleted, true, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrPollDeleted().sendFail(errorCode);
    }

    public static void sendLog(long memberId, long pollId, long teamId, long topicId) {
        new SprinklrPollDeleted()
                .setPropertyMemberId(memberId)
                .setPollId(pollId)
                .setTeamId(teamId)
                .setTopicId(topicId)
                .sendSuccess();
    }

    @Override
    public SprinklrPollDeleted setPropertyMemberId(long memberId) {
        setProperty(PropertyKey.MemberId, memberId);
        return this;
    }

    @Override
    public SprinklrPollDeleted setPollId(long pollId) {
        setProperty(PropertyKey.PollId, pollId);
        return this;
    }

    @Override
    public SprinklrPollDeleted setTeamId(long teamId) {
        setProperty(PropertyKey.TeamId, teamId);
        return this;
    }

    @Override
    public SprinklrPollDeleted setTopicId(long topicId) {
        setProperty(PropertyKey.TopicId, topicId);
        return this;
    }

}
