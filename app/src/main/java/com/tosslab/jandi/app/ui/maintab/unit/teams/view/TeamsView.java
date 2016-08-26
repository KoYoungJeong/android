package com.tosslab.jandi.app.ui.maintab.unit.teams.view;

import com.tosslab.jandi.app.ui.team.select.to.Team;

import java.util.List;

/**
 * Created by tonyjs on 16. 3. 21..
 */
public interface TeamsView {

    void setTeams(List<Team> second);

    void showAnotherTeamHasMessageMetaphor();

    void hideAnotherTeamHasMessageMetaphor();

    void clearTeams();

    void showProgressWheel();

    void dismissProgressWheel();

    void removePendingTeam(Team team);

    void moveToSelectTeam();

    void showTeamInviteIgnoreFailToast(String errorMessage);

    void showTeamInviteAcceptFailDialog(String errorMessage, Team team);

}
