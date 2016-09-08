package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.presenter;


public interface DeptJobPresenter {
    void onCreate();

    void onDestroy();

    void onSearchKeyword(String text);

    void onPickUser(long userId);

    void onItemClick(int position);

    interface View {
        void refreshDataView();

        void moveDirectMessage(long teamId, long userId, long roomId, long lastLinkId);

        void dismissEmptyView();

        void showEmptyView(String keyword);
    }
}
