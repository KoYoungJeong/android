package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.deptgroup.presenter;


public interface DeptJobGroupPresenter {
    void onCreate();

    void onMemberClick(int position);

    void onUnselectClick();

    void onAddClick();

    void onRefresh();

    void addToggleOfAll();

    interface View {
        void refreshDataView();

        void pickUser(long userId);

        void updateToggledUser(int count);

        void comeWithResult(long[] toggledUser);

        void onAddAllUser();
    }
}
