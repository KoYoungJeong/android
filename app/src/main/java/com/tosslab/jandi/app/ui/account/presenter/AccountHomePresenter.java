package com.tosslab.jandi.app.ui.account.presenter;

import com.tosslab.jandi.app.ui.team.select.to.Team;

import java.util.ArrayList;

/**
 * Created by Steve SeongUg Jung on 15. 3. 3..
 */
public interface AccountHomePresenter {

    void setView(View view);

    void onJoinedTeamSelect(Team clickedTeam);

    void onCreateTeamSelect();

    void onAccountNameEditClick(String oldName);

    void onChangeName(String inputMessage);

    public interface View {

        void showErrorToast(String message);

        void setTeamInfo(ArrayList<Team> result);

        void loadTeamCreateActivity();

        void showNameEditDialog(String oldName);
    }
}
