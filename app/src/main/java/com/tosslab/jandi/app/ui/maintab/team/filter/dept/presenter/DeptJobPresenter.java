package com.tosslab.jandi.app.ui.maintab.team.filter.dept.presenter;


public interface DeptJobPresenter {
    void onCreate();

    void onDestroy();

    void onSearchKeyword(String text);

    interface View {
        void refreshDataView();
    }
}
