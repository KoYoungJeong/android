package com.tosslab.jandi.app.events.team.invite;

import com.tosslab.jandi.app.ui.team.select.to.Team;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class TeamInviteAcceptEvent {
    private final Team team;

    private TeamInviteAcceptEvent(Team team) {
        this.team = team;
    }

    public static TeamInviteAcceptEvent create(Team team) {
        return new TeamInviteAcceptEvent(team);
    }

    public Team getTeam() {
        return team;
    }
}
