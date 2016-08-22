package com.tosslab.jandi.app.ui.maintab.team.filter.dept.presenter;


public interface DeptJobPresenter {
    void onCreate();

    void onDestroy();

    void onSearchKeyword(String text);

    void onPickUser(long userId);

    interface View {
        void refreshDataView();

        void moveDirectMessage(long teamId, long userId, long roomId, long lastLinkId);
    }
}
