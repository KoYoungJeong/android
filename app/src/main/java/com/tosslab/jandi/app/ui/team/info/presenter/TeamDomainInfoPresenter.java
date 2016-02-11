package com.tosslab.jandi.app.ui.team.info.presenter;

public interface TeamDomainInfoPresenter {
    void setView(View view);

    void checkEmailInfo();

    void createTeam(String teamName, String teamDomain);

    interface View {

        void failCreateTeam(int statusCode);

        void successCreateTeam(String name);

        void showProgressWheel();

        void dismissProgressWheel();

        void showFailToast(String message);

        void finishView();
    }
}
