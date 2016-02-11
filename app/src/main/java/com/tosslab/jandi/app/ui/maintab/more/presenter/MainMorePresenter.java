package com.tosslab.jandi.app.ui.maintab.more.presenter;

import android.net.Uri;
import android.text.SpannableStringBuilder;

public interface MainMorePresenter {
    void setView(View view);

    void showJandiVersion();

    void showOtherTeamMessageCount();

    void showTeamMember();

    void showUserProfile();

    void onLaunchHelpPage();

    void onReportUserInfo();

    interface View {

        void showUserProfile(Uri uri);

        void setVersionButtonVisibility(int visibility);

        void setJandiVersion(String version);

        void setOtherTeamBadgeCount(int badgeCount);

        void setMemberTextWithCount(String fullTeamMemberText);

        void launchHelpPageOnBrowser(String supportUrl);

        void setLatestVersion(int latestVersionCode);

        void showBugReportDialog(SpannableStringBuilder userInfoSpans, String userName);
    }
}
