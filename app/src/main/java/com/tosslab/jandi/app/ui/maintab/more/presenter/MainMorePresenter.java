package com.tosslab.jandi.app.ui.maintab.more.presenter;

import android.net.Uri;
import android.text.SpannableStringBuilder;

import com.tosslab.jandi.app.ui.maintab.more.domain.VersionClickedInfo;

public interface MainMorePresenter {
    void setView(View view);

    void onShowJandiVersion();

    void onShowOtherTeamMessageCount();

    void onShowTeamMember();

    void onShowUserProfile();

    void onLaunchHelpPage();

    void onReportUserInfo(VersionClickedInfo versionClickInfo);

    interface View {

        void showUserProfile(Uri uri);

        void setVersionButtonVisibility(int visibility);

        void setJandiVersion(String version);

        void setOtherTeamBadgeCount(int badgeCount);

        void setMemberTextWithCount(String fullTeamMemberText);

        void launchHelpPageOnBrowser(String supportUrl);

        void setLatestVersion(int latestVersionCode);

        void showBugReportDialog(SpannableStringBuilder userInfoSpans);
    }
}
