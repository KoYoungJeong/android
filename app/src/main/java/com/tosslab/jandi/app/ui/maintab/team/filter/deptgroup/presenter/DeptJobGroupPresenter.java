package com.tosslab.jandi.app.ui.maintab.team.filter.deptgroup.presenter;


public interface DeptJobGroupPresenter {
    void onCreate();

    void onDestroy();

    void onMemberClick(int position);

    void onUnselectClick();

    void onAddClick();

    interface View {
        void refreshDataView();

        void pickUser(long userId);

        void updateToggledUser(int count);

        void comeWithResult(long[] toggledUser);
    }
}
