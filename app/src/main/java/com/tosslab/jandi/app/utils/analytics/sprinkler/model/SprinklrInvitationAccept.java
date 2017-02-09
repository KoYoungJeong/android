package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.ITeamId;

/**
 * Created by tee on 2017. 2. 9..
 */

public class SprinklrInvitationAccept extends MainSprinklrModel
        implements ITeamId {
    private SprinklrInvitationAccept() {
        super(SprinklerEvents.TeamInvitationAccept, false, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrInvitationAccept().sendFail(errorCode);
    }

    public static void sendLog(long teamId) {
        new SprinklrInvitationAccept()
                .setTeamId(teamId)
                .sendSuccess();
    }

    @Override
    public MainSprinklrModel setTeamId(long teamId) {
        setProperty(PropertyKey.TeamId, teamId);
        return this;
    }
}
