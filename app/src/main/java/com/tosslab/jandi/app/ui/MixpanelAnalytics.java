package com.tosslab.jandi.app.ui;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.team.TeamInfoLoader;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class MixpanelAnalytics {

    private MixpanelMemberAnalyticsClient mMixpanelMemberAnalyticsClient;


    public void trackSigningIn() {
        String distictId = getDistictId();
        if (!TextUtils.isEmpty(distictId)) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(JandiApplication.getContext(), distictId);
            mMixpanelMemberAnalyticsClient.trackMemberSingingIn();
        }
    }

    protected void onDestroy() {
        if (mMixpanelMemberAnalyticsClient != null)
            mMixpanelMemberAnalyticsClient.flush();
    }

    private String getDistictId() {
        try {
            long teamId = TeamInfoLoader.getInstance().getTeamId();
            long myId = TeamInfoLoader.getInstance().getMyId();
            return myId + "-" + teamId;
        } catch (Exception e) {
            return "";
        }

    }
}
