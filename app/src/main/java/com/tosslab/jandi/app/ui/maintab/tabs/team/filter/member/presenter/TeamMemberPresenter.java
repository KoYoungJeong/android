package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.presenter;


public interface TeamMemberPresenter {

    void onCreate();

    void onDestroy();

    void onItemClick(int position);

    void onSearchKeyword(String text);

    void addToggledUser(long[] users);

    void addToggleOfAll();

    void onUserSelect(long userId);

    void clearToggle();

    void inviteToggle();

    interface View {

        void moveDisabledMembers();

        void refreshDataView();

        void moveProfile(long userId);

        void updateToggledUser(int toggledSize);

        void moveDirectMessage(long teamId, long userId, long roomId, long lastLinkId);

        void showPrgoress();

        void dismissProgress();

        void successToInvitation();

        void showFailToInvitation();

        void showEmptyView(String keyword);

        void dismissEmptyView();
    }
}
