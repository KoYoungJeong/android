package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IPropertyValueMemberId;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.ITeamId;

/**
 * Created by tee on 2017. 2. 9..
 */

public class SprinklrInvitationAccept extends MainSprinklrModel
        implements ITeamId, IPropertyValueMemberId {
    private SprinklrInvitationAccept() {
        super(SprinklerEvents.TeamInvitationAccept, true, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrInvitationAccept().sendFail(errorCode);
    }

    public static void sendLog(long teamId, long memberId) {
        new SprinklrInvitationAccept()
                .setTeamId(teamId)
                .setPropertyMemberId(memberId)
                .sendSuccess();
    }

    @Override
    public SprinklrInvitationAccept setTeamId(long teamId) {
        setProperty(PropertyKey.TeamId, teamId);
        return this;
    }

    @Override
    public SprinklrInvitationAccept setPropertyMemberId(long memberId) {
        setProperty(PropertyKey.MemberId, memberId);
        return this;
    }
}
