package com.tosslab.jandi.app.utils.analytics.sprinkler.model;


import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IMemberCount;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IRankId;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.ITeamId;

public class SprinklrInvitationTeam extends MainSprinklrModel
        implements ITeamId, IMemberCount, IRankId {
    private SprinklrInvitationTeam() {
        super(SprinklerEvents.InviteTeam, false, false);
    }

    public static void sendLog(long teamId, int memberCount, long rankId) {
        new SprinklrInvitationTeam()
                .setTeamId(teamId)
                .setMemberCount(memberCount)
                .setRankId(rankId)
                .send();
    }

    @Override
    public SprinklrInvitationTeam setTeamId(long teamId) {
        setProperty(PropertyKey.TeamId, teamId);
        return this;
    }

    @Override
    public SprinklrInvitationTeam setMemberCount(int count) {
        setProperty(PropertyKey.MemberCount, count);
        return this;
    }

    @Override
    public SprinklrInvitationTeam setRankId(long rankId) {
        setProperty(PropertyKey.RankdId, rankId);
        return this;
    }
}
