package com.tosslab.jandi.app.ui.team.select.presenter;

import android.content.DialogInterface;

import com.tosslab.jandi.app.ui.team.select.to.Team;

/**
 * Created by tee on 2016. 9. 27..
 */

public interface TeamSelectListPresenter {

    void initTeamDatas(boolean firstEntered, boolean shouldRefreshAccountInfo);

    void onEnterSelectedTeam(long teamId);

    void onRequestAcceptJoin(Team selectedTeam);

    void onRequestIgnoreJoin(Team selectedTeam, boolean showErrorToast);

    void setUserEmailInfo();

    interface View {
        void justRefresh();

        void showEmptyList();

        void showList();

        void showProgressWheel();

        void dismissProgressWheel();

        void moveSelectedTeam();

        void moveCreateTeam(boolean isFirstEntered);

        void showTextAlertDialog(String msg, DialogInterface.OnClickListener clickListener);

        void showErrorToast(String message);

        void showLoginEmail(String email);
    }
}
