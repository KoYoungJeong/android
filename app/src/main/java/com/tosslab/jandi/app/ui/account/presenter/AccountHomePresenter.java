package com.tosslab.jandi.app.ui.account.presenter;

import android.content.DialogInterface;

import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 3. 3..
 */
public interface AccountHomePresenter {

    void setView(View view);

    void onInitialize(boolean shouldRefreshAccountInfo);

    void onJoinedTeamSelect(long teamId, boolean firstJoin);

    void onCreateTeamSelect();

    void onAccountNameEditClick(String oldName);

    void onChangeName(String inputMessage);

    void onTeamCreateAcceptResult();

    void onAccountEmailEditClick();

    void onEmailChooseResult();

    void onRequestJoin(Team selectedTeam);

    void onRequestIgnore(Team selectedTeam, boolean showErrorToast);

    void onHelpOptionSelect();

    interface View {

        void showErrorToast(String message);

        void setTeamInfo(List<Team> result, ResAccountInfo.UserTeam selectedTeamInfo);

        void loadTeamCreateActivity();

        void showNameEditDialog(String oldName);

        void showSuccessToast(String message);

        void setAccountName(String newName);

        void dismissProgressWheel();

        void showProgressWheel();

        void moveSelectedTeam(boolean firstJoin);

        void moveEmailEditClick();

        void setUserEmailText(String email);

        void removePendingTeamView(Team selectedTeam);

        void showHelloDialog();

        void moveAfterInvitaionAccept();

        void showTextAlertDialog(String msg, DialogInterface.OnClickListener clickListener);

        void invalidAccess();

        void showCheckNetworkDialog();
    }
}
