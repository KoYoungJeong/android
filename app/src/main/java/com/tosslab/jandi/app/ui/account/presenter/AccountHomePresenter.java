package com.tosslab.jandi.app.ui.account.presenter;

import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import java.util.ArrayList;

/**
 * Created by Steve SeongUg Jung on 15. 3. 3..
 */
public interface AccountHomePresenter {

    void setView(View view);

    void onJoinedTeamSelect(int teamId);

    void onCreateTeamSelect();

    void onAccountNameEditClick(String oldName);

    void onChangeName(String inputMessage);

    void onTeamCreateResult();

    void onAccountEmailEditClick();

    void onEmailChooseResult();

    void onRequestJoin(Team selectedTeam);

    void onRequestIgnore(Team selectedTeam);

    void onHelpOptionSelect();

    public interface View {

        void showErrorToast(String message);

        void setTeamInfo(ArrayList<Team> result, ResAccountInfo.UserTeam selectedTeamInfo);

        void loadTeamCreateActivity();

        void showNameEditDialog(String oldName);

        void showSuccessToast(String message);

        void setAccountName(String newName);

        void dismissProgressWheel();

        void showProgressWheel();

        void moveSelectedTeam();

        void moveEmailEditClick();

        void setUserEmailText(String email);

        void moveCreatedTeamDomain(Team selectedTeam);

        void removePendingTeamView(Team selectedTeam);

        void showHeloDialog();
    }
}