package com.tosslab.jandi.app.ui.maintab.teams.view;

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

    void moveToSelectTeam();

    void removePendingTeam(Team team);

    void showTeamInviteIgnoreFailToast(String errorMessage);

    void showTeamInviteAcceptFailDialog(String errorMessage, Team team);

}
