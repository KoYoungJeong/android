package com.tosslab.jandi.app.ui.maintab.more.presenter;

import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
import com.tosslab.jandi.app.ui.maintab.more.domain.VersionClickedInfo;
import com.tosslab.jandi.app.ui.maintab.more.model.MainMoreModel;
import com.tosslab.jandi.app.utils.BadgeUtils;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

@EBean
public class MainMorePresenterImpl implements MainMorePresenter {
    @Bean
    MainMoreModel mainMoreModel;
    private View view;

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Background
    @Override
    public void onShowJandiVersion() {
        String versionName = mainMoreModel.getVersionName();
        if (!TextUtils.isEmpty(versionName)) {
            view.setJandiVersion(String.format("(v%s)", versionName));
        }

        if (mainMoreModel.isConnectedNetwork()) {
            Pair<Boolean, Integer> updateInfo = mainMoreModel.needToUpdate();
            if (updateInfo.first) {
                view.setLatestVersion(updateInfo.second);
                view.setVersionButtonVisibility(android.view.View.VISIBLE);
            } else {
                view.setVersionButtonVisibility(android.view.View.GONE);
            }
        } else {
            view.setVersionButtonVisibility(android.view.View.GONE);
        }


    }

    @Override
    public void onShowOtherTeamMessageCount() {
        int badgeCount = mainMoreModel.getOtherTeamBadge();
        BadgeUtils.setBadge(JandiApplication.getContext(), BadgeCountRepository.getRepository().getTotalBadgeCount());
        view.setOtherTeamBadgeCount(badgeCount);
    }

    @Override
    public void onShowTeamMember() {
        String teamMember = JandiApplication.getContext().getString(R.string.jandi_team_member);
        int teamMemberCount = mainMoreModel.getEnabledUserCount();
        String fullTeamMemberText = String.format("%s\n(%d)", teamMember, teamMemberCount);
        view.setMemberTextWithCount(fullTeamMemberText);

    }

    @Override
    public void onShowUserProfile() {
        FormattedEntity me = EntityManager.getInstance().getMe();
        Uri uri = Uri.parse(me.getUserSmallProfileUrl());

        view.showUserProfile(uri);
    }

    @Override
    public void onLaunchHelpPage() {
        String supportUrl = mainMoreModel.getSupportUrlEachLanguage();
        view.launchHelpPageOnBrowser(supportUrl);
    }

    @Override
    public void onReportUserInfo(VersionClickedInfo versionClickInfo) {
        int count = versionClickInfo.getCount();
        if (count == 0) {
            versionClickInfo.setTime(System.currentTimeMillis());
        } else if (!mainMoreModel.isIn3Seconds(versionClickInfo.getTime())) {
            count = 0;
            versionClickInfo.setTime(System.currentTimeMillis());
        }
        versionClickInfo.setCount(++count);

        if (versionClickInfo.getCount() >= 5) {
            versionClickInfo.setCount(0);
            List<Pair<String, String>> userInfosForBugReport = mainMoreModel.getUserInfosForBugReport();
            SpannableStringBuilder userInfoSpans = mainMoreModel.getUserInfoSpans(userInfosForBugReport);
            view.showBugReportDialog(userInfoSpans);
        }
    }

}
