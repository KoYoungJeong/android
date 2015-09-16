package com.tosslab.jandi.app.ui;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.json.JSONException;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class MixpanelAnalytics {

    private boolean mDoneAnalizeTrackingSignIn = false;      // 로그인 상황을 MIXPANEL
    private MixpanelMemberAnalyticsClient mMixpanelMemberAnalyticsClient;


    public void trackSigningIn(EntityManager entityManager) {
        if (entityManager != null && !mDoneAnalizeTrackingSignIn) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(JandiApplication.getContext(), entityManager.getDistictId());
            mMixpanelMemberAnalyticsClient.trackMemberSingingIn();
            mDoneAnalizeTrackingSignIn = true;
        }
    }

    public void trackDownloadingFile(EntityManager entityManager, ResMessages.FileMessage fileInfo) {
        if (entityManager != null) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(JandiApplication.getContext(), entityManager.getDistictId());
            try {
                mMixpanelMemberAnalyticsClient.trackDownloadFile(fileInfo);
            } catch (JSONException e) {
                LogUtil.e("CANNOT MEET", e);
            }
        }
    }

    public void trackSharingFile(EntityManager entityManager,
                                 int entityType, ResMessages.FileMessage fileInfo) {
        if (entityManager != null) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(JandiApplication.getContext(), entityManager.getDistictId());
            try {
                mMixpanelMemberAnalyticsClient.trackSharingFile(entityType, fileInfo);
            } catch (JSONException e) {
                LogUtil.e("CANNOT MEET", e);
            }
        }
    }

    public void trackUnsharingFile(EntityManager entityManager,
                                   int entityType, ResMessages.FileMessage fileInfo) {
        if (entityManager != null) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(JandiApplication.getContext(), entityManager.getDistictId());
            try {
                mMixpanelMemberAnalyticsClient.trackUnsharingFile(entityType, fileInfo);
            } catch (JSONException e) {
                LogUtil.e("CANNOT MEET", e);
            }
        }
    }

    /**
     * ProfileActivity에서는 entityManager가 없기 때문에 distictId를 그대로 가져온다.
     *
     * @param distictId
     * @param updatedMyProfile
     */
    public void trackUpdateProfile(String distictId, ResLeftSideMenu.User updatedMyProfile) {
        if (distictId != null) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(JandiApplication.getContext(), distictId);
            try {
                mMixpanelMemberAnalyticsClient.trackProfile(updatedMyProfile);
            } catch (JSONException e) {
                LogUtil.e("CANNOT MEET", e);
            }
        }
    }

    protected void onDestroy() {
        if (mMixpanelMemberAnalyticsClient != null)
            mMixpanelMemberAnalyticsClient.flush();
    }
}
