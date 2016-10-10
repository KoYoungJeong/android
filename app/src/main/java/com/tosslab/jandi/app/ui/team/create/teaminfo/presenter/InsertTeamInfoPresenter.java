package com.tosslab.jandi.app.ui.team.create.teaminfo.presenter;

/**
 * Created by tee on 16. 6. 24..
 */
public interface InsertTeamInfoPresenter {
    void checkEmailInfo();

    void createTeam(String teamName, String teamDomain, int mode);

    interface View {

        void showTeamNameLengthError();

        void showTeamDomainInvalidUrlError();

        void showTeamDomainLengthError();

        void showTeamInvalidOrSameDomainError();

        void showProgressWheel();

        void dismissProgressWheel();

        void showFailToast(String message);

        void failCreateTeam(int statusCode);

        void onMoveInsertProfilePage();

        void onMoveMainTabActivity();

        void finish();
    }
}
