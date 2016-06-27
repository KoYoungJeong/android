package com.tosslab.jandi.app.ui.maintab.teams.presenter;

import com.tosslab.jandi.app.ui.maintab.team.presenter.TeamPresenter;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import java.util.List;

/**
 * Created by tonyjs on 16. 3. 21..
 */
public interface TeamsPresenter {

    String TAG = TeamPresenter.class.getSimpleName();

    void initializeTeamInitializeQueue();

    void onInitializeTeams();

    void reInitializeTeams();

    void determineAnotherTeamHasMessage(long selectedTeamId, List<Team> teams);

    void onTeamJoinAction(long teamId);

    void onTeamInviteAcceptAction(Team team);

    void onTeamInviteIgnoreAction(Team team);

    void onTeamCreated();

    void clearTeamInitializeQueue();
}
