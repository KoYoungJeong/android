package com.tosslab.jandi.app.ui;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.JsonObject;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.MixpanelAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.apache.log4j.Logger;
import org.json.JSONException;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class BaseAnalyticsActivity extends Activity {
    private static String GA_PATH_FILE_DETAIL     = "File Detail";
    private static String GA_PATH_PROFILE         = "Profile";
    private static String GA_PATH_TEAM_INFO       = "Team Info";

    private static String GA_PATH_CHANNEL         = "Topic";
    private static String GA_PATH_DIRECT_MESSAGE  = "Direct Message";
    private static String GA_PATH_PRIVATE_GROUP   = "Private Group";


    private static String GA_PATH_CHANNEL_PANEL         = "Channel Panel";
    private static String GA_PATH_DIRECT_MESSAGE_PANEL  = "Direct Message Panel";
    private static String GA_PATH_PRIVATE_GROUP_PANEL   = "Private Group Panel";
    private static String GA_PATH_FILE_PANEL            = "File Panel";

    private final Logger log = Logger.getLogger(BaseAnalyticsActivity.class);

    private boolean mDoneAnalizeTrackingSignIn = false;      // 로그인 상황을 MIXPANEL
    private MixpanelAnalyticsClient mMixpanelAnalyticsClient;

    protected void returnToIntroStartActivity() {
        JandiPreference.clearMyToken(this);
        Intent intent = new Intent(this, IntroActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    protected void trackSigningIn(EntityManager entityManager) {
        if (entityManager != null && !mDoneAnalizeTrackingSignIn) {
            mMixpanelAnalyticsClient = MixpanelAnalyticsClient.getInstance(this, entityManager.getDistictId());
            mMixpanelAnalyticsClient.trackSingingIn();
            mDoneAnalizeTrackingSignIn = true;
        }
    }

    protected void trackSigningInFromPush(EntityManager entityManager) {
        if (entityManager != null) {
            mMixpanelAnalyticsClient = MixpanelAnalyticsClient.getInstance(this, entityManager.getDistictId());
            mMixpanelAnalyticsClient.trackSingingIn();
        }
    }

    protected void trackInvitingToEntity(EntityManager entityManager, int entityType) {
        if (entityManager != null) {
            mMixpanelAnalyticsClient = MixpanelAnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mMixpanelAnalyticsClient.trackInvitingToEntity(entityType == JandiConstants.TYPE_TOPIC);
            } catch (JSONException e) {
                log.error("CANNOT MEET", e);
            }
        }
    }

    protected void trackDeletingEntity(EntityManager entityManager, int entityType) {
        if (entityManager != null) {
            mMixpanelAnalyticsClient = MixpanelAnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mMixpanelAnalyticsClient.trackDeletingEntity(entityType == JandiConstants.TYPE_TOPIC);
            } catch (JSONException e) {
                log.error("CANNOT MEET", e);
            }
        }
    }

    protected void trackChangingEntityName(EntityManager entityManager, int entityType) {
        if (entityManager != null) {
            mMixpanelAnalyticsClient = MixpanelAnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mMixpanelAnalyticsClient.trackChangingEntityName(entityType == JandiConstants.TYPE_TOPIC);
            } catch (JSONException e) {
                log.error("CANNOT MEET", e);
            }
        }
    }

    protected void trackLeavingEntity(EntityManager entityManager, int entityType) {
        if (entityManager != null) {
            mMixpanelAnalyticsClient = MixpanelAnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mMixpanelAnalyticsClient.trackLeavingEntity(entityType == JandiConstants.TYPE_TOPIC);
            } catch (JSONException e) {
                log.error("CANNOT MEET", e);
            }
        }
    }

    protected void trackUploadingFile(EntityManager entityManager,
                                      int entityType, JsonObject fileInfo) {
        if (entityManager != null) {
            mMixpanelAnalyticsClient = MixpanelAnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mMixpanelAnalyticsClient.trackUploadingFile(entityType, fileInfo);
            } catch (JSONException e) {
                log.error("CANNOT MEET", e);
            }
        }
    }

    protected void trackDownloadingFile(EntityManager entityManager, ResMessages.FileMessage fileInfo) {
        if (entityManager != null) {
            mMixpanelAnalyticsClient = MixpanelAnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mMixpanelAnalyticsClient.trackDownloadFile(fileInfo);
            } catch (JSONException e) {
                log.error("CANNOT MEET", e);
            }
        }
    }

    protected void trackSharingFile(EntityManager entityManager,
                                    int entityType, ResMessages.FileMessage fileInfo) {
        if (entityManager != null) {
            mMixpanelAnalyticsClient = MixpanelAnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mMixpanelAnalyticsClient.trackSharingFile(entityType, fileInfo);
            } catch (JSONException e) {
                log.error("CANNOT MEET", e);
            }
        }
    }

    protected void trackUnsharingFile(EntityManager entityManager,
                                    int entityType, ResMessages.FileMessage fileInfo) {
        if (entityManager != null) {
            mMixpanelAnalyticsClient = MixpanelAnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mMixpanelAnalyticsClient.trackUnsharingFile(entityType, fileInfo);
            } catch (JSONException e) {
                log.error("CANNOT MEET", e);
            }
        }
    }

    /**
     * ProfileActivity에서는 entityManager가 없기 때문에 distictId를 그대로 가져온다.
     * @param distictId
     * @param updatedMyProfile
     */
    protected void trackUpdateProfile(String distictId, ResLeftSideMenu.User updatedMyProfile) {
        if (distictId != null) {
            mMixpanelAnalyticsClient = MixpanelAnalyticsClient.getInstance(this, distictId);
            try {
                mMixpanelAnalyticsClient.trackProfile(updatedMyProfile);
            } catch (JSONException e) {
                log.error("CANNOT MEET", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mMixpanelAnalyticsClient != null)
            mMixpanelAnalyticsClient.flush();
        super.onDestroy();
    }


    /*************************
     * Google Analytics
     */
    protected void trackGaFileDetail(EntityManager entityManager) {
        if (entityManager == null) return;
        trackGa(entityManager.getDistictId(), GA_PATH_FILE_DETAIL);
    }

    protected void trackGaProfile(final String distictId) {
        trackGa(distictId, GA_PATH_PROFILE);
    }

    protected void trackTeamInfo(final String districtId) {
        trackGa(districtId, GA_PATH_TEAM_INFO);
    }

    protected void trackGaMessageList(EntityManager entityManager, int entityType) {
        if (entityManager == null) return;
        String gaPath = (entityType == JandiConstants.TYPE_TOPIC) ? GA_PATH_CHANNEL
                : (entityType == JandiConstants.TYPE_DIRECT_MESSAGE) ? GA_PATH_DIRECT_MESSAGE
                : GA_PATH_PRIVATE_GROUP;
        trackGa(entityManager.getDistictId(), gaPath);
    }

    protected void trackGaTab(EntityManager entityManager, int tabPosition) {
        if (entityManager == null) return;

        String gaPath;
        switch (tabPosition) {
            case 0:
                gaPath = GA_PATH_CHANNEL_PANEL;
                break;
            case 1:
                gaPath = GA_PATH_DIRECT_MESSAGE_PANEL;
                break;
            case 2:
                gaPath = GA_PATH_PRIVATE_GROUP_PANEL;
                break;
            default:
                gaPath = GA_PATH_FILE_PANEL;
                break;
        }

        trackGa(entityManager.getDistictId(), gaPath);
    }

    private void trackGa(final String distictId, final String gaPath) {
        Tracker screenViewTracker = ((JandiApplication) getApplication())
                .getTracker(JandiApplication.TrackerName.APP_TRACKER);
        screenViewTracker.set("&uid", distictId);
        screenViewTracker.setScreenName(gaPath);
        screenViewTracker.send(new HitBuilders.AppViewBuilder().build());

        Tracker screenViewGlobalTracker = ((JandiApplication) getApplication())
                .getTracker(JandiApplication.TrackerName.GLOBAL_TRACKER);
        screenViewGlobalTracker.set("&uid", distictId);
        screenViewGlobalTracker.setScreenName(gaPath);
        screenViewGlobalTracker.send(new HitBuilders.AppViewBuilder().build());
    }
}
