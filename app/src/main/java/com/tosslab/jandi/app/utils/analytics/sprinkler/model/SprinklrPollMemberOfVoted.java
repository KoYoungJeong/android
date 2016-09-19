package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IPollId;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.ITeamId;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrPollMemberOfVoted extends MainSprinklrModel
        implements ITeamId, IPollId {

    private SprinklrPollMemberOfVoted() {
        super(SprinklerEvents.PollMemberOfVoted, true, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrPollMemberOfVoted().sendFail(errorCode);
    }

    public static void sendLog(long pollId, long teamId) {
        new SprinklrPollMemberOfVoted()
                .setPollId(pollId)
                .setTeamId(teamId)
                .sendSuccess();
    }

    @Override
    public SprinklrPollMemberOfVoted setPollId(long pollId) {
        setProperty(PropertyKey.PollId, pollId);
        return this;
    }

    @Override
    public SprinklrPollMemberOfVoted setTeamId(long teamId) {
        setProperty(PropertyKey.TeamId, teamId);
        return this;
    }

}
