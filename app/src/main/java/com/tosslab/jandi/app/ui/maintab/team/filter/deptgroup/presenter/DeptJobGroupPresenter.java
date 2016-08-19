package com.tosslab.jandi.app.ui.maintab.team.filter.deptgroup.presenter;


public interface DeptJobGroupPresenter {
    void onCreate();

    void onDestroy();

    void onMemberClick(int position);

    interface View {
        void refreshDataView();

        void moveMemberProfile(long userId);
    }
}
