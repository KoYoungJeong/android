package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.ITeamId;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrCreateTeam extends MainSprinklrModel
        implements ITeamId {

    private SprinklrCreateTeam() {
        super(SprinklerEvents.CreateTeam, true, false);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrCreateTeam().sendFail(errorCode);
    }

    public static void sendLog(long teamId) {
        new SprinklrCreateTeam()
                .setTeamId(teamId)
                .sendSuccess();
    }

    @Override
    public MainSprinklrModel setTeamId(long teamId) {
        setProperty(PropertyKey.TeamId, teamId);
        return this;
    }
}