package com.tosslab.jandi.app.ui.maintab.navigation.presenter;

import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.team.select.to.Team;

/**
 * Created by tonyjs on 2016. 8. 17..
 */
public interface NavigationPresenter {

    String TAG = NavigationPresenter.class.getSimpleName();

    void initializeTeamInitializeQueue();

    void onInitializeTeams();

    void reInitializeTeams();

    void onTeamJoinAction(long teamId);

    void onTeamInviteAcceptAction(Team team);

    void onTeamInviteIgnoreAction(Team team);

    void onTeamCreated();

    void clearTeamInitializeQueue();

    void onInitializePresetNavigationItems();

    void onSignOutAction();

    void onInitJandiVersion();

    void onInitOrientations();

    void onLaunchHelpPage();

    void onInitUserProfile();

    void onSetUpOrientation(String selectedValue);

    interface View {

        void showCheckNetworkDialog();

        void showSuccessToast(String message);

        void showProgressWheel();

        void dismissProgressWheel();

        void setOrientationViewVisibility(boolean show);

        void setOrientation(int orientation);

        void setOrientationSummary(String value);

        void moveLoginActivity();

        void setVersion(String version);

        void launchHelpPage(String supportUrl);

        void moveToSelectTeam();

        void showTeamInviteIgnoreFailToast(String errorMessage);

        void showTeamInviteAcceptFailDialog(String errorMessage, Team team);

        void notifyDataSetChanged();

        void setUserProfile(User user);
    }

}
