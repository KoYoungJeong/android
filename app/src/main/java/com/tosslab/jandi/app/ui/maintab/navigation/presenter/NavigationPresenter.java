package com.tosslab.jandi.app.ui.maintab.navigation.presenter;

import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import java.util.List;

/**
 * Created by tonyjs on 2016. 8. 17..
 */
public interface NavigationPresenter {

    String TAG = NavigationPresenter.class.getSimpleName();

    void initializeTeamInitializeQueue();

    void initBadgeCount();

    void initLauncherBadgeCount(List<Team> teams);

    void onInitializeTeams();

    void onTeamJoinAction(long teamId);

    void onTeamInviteAcceptAction(Team team);

    void onTeamInviteIgnoreAction(Team team);

    void onTeamCreated();

    void clearTeamInitializeQueue();

    void clearBadgeCountingQueue();

    void onInitializePresetNavigationItems();

    void onSignOutAction();

    void onLaunchHelpPage();

    void onInitUserProfile();

    void initializeBadgeCountingQueue();

    void onMessageRead(boolean fromSelf, long teamId, int readCount);

    void onInitIntercom();

    void onReloadTeams();

    interface View {

        void showCheckNetworkDialog();

        void showSuccessToast(String message);

        void showProgressWheel();

        void dismissProgressWheel();

        void moveLoginActivity();

        void launchHelpPage(String supportUrl);

        void moveToSelectTeam();

        void showTeamInviteIgnoreFailToast(String errorMessage);

        void showTeamInviteAcceptFailDialog(String errorMessage, Team team);

        void notifyDataSetChanged();

        void setUserProfile(User user);

        void closeNavigation();

        void moveTeamList();
    }

}
