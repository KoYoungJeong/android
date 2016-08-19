package com.tosslab.jandi.app.ui.maintab.team.filter.member.presenter;


public interface TeamMemberPresenter {

    void onCreate();

    void onDestroy();

    void onItemClick(int position);

    void onSearchKeyword(String text);

    interface View {

        void refreshDataView();

        void moveProfile(long userId);
    }
}
