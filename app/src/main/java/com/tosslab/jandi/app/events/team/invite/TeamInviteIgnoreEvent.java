package com.tosslab.jandi.app.events.team.invite;

import com.tosslab.jandi.app.ui.team.select.to.Team;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class TeamInviteIgnoreEvent {
    private final Team team;

    private TeamInviteIgnoreEvent(Team team) {
        this.team = team;
    }

    public static TeamInviteIgnoreEvent create(Team team) {
        return new TeamInviteIgnoreEvent(team);
    }

    public Team getTeam() {
        return team;
    }
}
