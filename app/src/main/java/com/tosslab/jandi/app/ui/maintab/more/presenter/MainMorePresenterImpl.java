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
    private int versionClickCount = 0;
    private long versionClickTime;

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Background
    @Override
    public void showJandiVersion() {
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
    public void showOtherTeamMessageCount() {
        int badgeCount = mainMoreModel.getOtherTeamBadge();
        BadgeUtils.setBadge(JandiApplication.getContext(), BadgeCountRepository.getRepository().getTotalBadgeCount());
        view.setOtherTeamBadgeCount(badgeCount);
    }

    @Override
    public void showTeamMember() {
        String teamMember = JandiApplication.getContext().getString(R.string.jandi_team_member);
        int teamMemberCount = mainMoreModel.getEnabledUserCount();
        String fullTeamMemberText = String.format("%s\n(%d)", teamMember, teamMemberCount);
        view.setMemberTextWithCount(fullTeamMemberText);

    }

    @Override
    public void showUserProfile() {
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
    public void onReportUserInfo() {
        if (versionClickCount == 0) {
            versionClickTime = System.currentTimeMillis();
        } else if (!mainMoreModel.isIn3Seconds(versionClickTime)) {
            versionClickCount = 0;
            versionClickTime = System.currentTimeMillis();
        }
        ++versionClickCount;

        if (versionClickCount >= 5) {
            versionClickCount = 0;
            List<Pair<String, String>> userInfosForBugReport = mainMoreModel.getUserInfosForBugReport();
            SpannableStringBuilder userInfoSpans = mainMoreModel.getUserInfoSpans(userInfosForBugReport);
            view.showBugReportDialog(userInfoSpans, EntityManager.getInstance().getMe().getName());
        }
    }

}
