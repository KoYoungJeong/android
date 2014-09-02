package com.tosslab.jandi.app.ui;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.JsonObject;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.AnalyticsClient;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.apache.log4j.Logger;
import org.json.JSONException;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class BaseActivity extends Activity {
    private static String GA_PATH_FILE_DETAIL     = "File Detail";
    private static String GA_PATH_PROFILE         = "Profile";
    private static String GA_PATH_SETTING         = "Setting";

    private static String GA_PATH_CHANNEL         = "Channel";
    private static String GA_PATH_DIRECT_MESSAGE  = "Direct Message";
    private static String GA_PATH_PRIVATE_GROUP   = "Private Channel";

    private static String GA_PATH_CHANNEL_PANEL         = "Channel Panel";
    private static String GA_PATH_DIRECT_MESSAGE_PANEL  = "Direct Message Panel";
    private static String GA_PATH_PRIVATE_GROUP_PANEL   = "Private Channel Panel";
    private static String GA_PATH_FILE_PANEL            = "File Panel";

    private final Logger log = Logger.getLogger(BaseActivity.class);

    private boolean mDoneAnalizeTrackingSignIn = false;      // 로그인 상황을 MIXPANEL
    private AnalyticsClient mAnalyticsClient;

    protected void returnToLoginActivity() {
        JandiPreference.clearMyToken(this);
        Intent intent = new Intent(this, LoginActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    protected void trackSigningIn(EntityManager entityManager) {
        if (entityManager != null && !mDoneAnalizeTrackingSignIn) {
            mAnalyticsClient = AnalyticsClient.getInstance(this, entityManager.getDistictId());
            mAnalyticsClient.trackSingingIn();
            mDoneAnalizeTrackingSignIn = true;
        }
    }

    protected void trackSigningInFromPush(EntityManager entityManager) {
        if (entityManager != null) {
            mAnalyticsClient = AnalyticsClient.getInstance(this, entityManager.getDistictId());
            mAnalyticsClient.trackSingingIn();
        }
    }

    protected void trackInvitingToEntity(EntityManager entityManager, int entityType) {
        if (entityManager != null) {
            mAnalyticsClient = AnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mAnalyticsClient.trackInvitingToEntity(entityType == JandiConstants.TYPE_CHANNEL);
            } catch (JSONException e) {
                log.error("CANNOT MEET", e);
            }
        }
    }

    protected void trackDeletingEntity(EntityManager entityManager, int entityType) {
        if (entityManager != null) {
            mAnalyticsClient = AnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mAnalyticsClient.trackDeletingEntity(entityType == JandiConstants.TYPE_CHANNEL);
            } catch (JSONException e) {
                log.error("CANNOT MEET", e);
            }
        }
    }

    protected void trackChangingEntityName(EntityManager entityManager, int entityType) {
        if (entityManager != null) {
            mAnalyticsClient = AnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mAnalyticsClient.trackChangingEntityName(entityType == JandiConstants.TYPE_CHANNEL);
            } catch (JSONException e) {
                log.error("CANNOT MEET", e);
            }
        }
    }

    protected void trackLeavingEntity(EntityManager entityManager, int entityType) {
        if (entityManager != null) {
            mAnalyticsClient = AnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mAnalyticsClient.trackLeavingEntity(entityType == JandiConstants.TYPE_CHANNEL);
            } catch (JSONException e) {
                log.error("CANNOT MEET", e);
            }
        }
    }

    protected void trackUploadingFile(EntityManager entityManager,
                                      int entityType, JsonObject fileInfo) {
        if (entityManager != null) {
            mAnalyticsClient = AnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mAnalyticsClient.trackUploadingFile(entityType, fileInfo);
            } catch (JSONException e) {
                log.error("CANNOT MEET", e);
            }
        }
    }

    protected void trackDownloadingFile(EntityManager entityManager, ResMessages.FileMessage fileInfo) {
        if (entityManager != null) {
            mAnalyticsClient = AnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mAnalyticsClient.trackDownloadFile(fileInfo);
            } catch (JSONException e) {
                log.error("CANNOT MEET", e);
            }
        }
    }

    protected void trackSharingFile(EntityManager entityManager,
                                    int entityType, ResMessages.FileMessage fileInfo) {
        if (entityManager != null) {
            mAnalyticsClient = AnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mAnalyticsClient.trackSharingFile(entityType, fileInfo);
            } catch (JSONException e) {
                log.error("CANNOT MEET", e);
            }
        }
    }

    protected void trackUnsharingFile(EntityManager entityManager,
                                    int entityType, ResMessages.FileMessage fileInfo) {
        if (entityManager != null) {
            mAnalyticsClient = AnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mAnalyticsClient.trackUnsharingFile(entityType, fileInfo);
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
            mAnalyticsClient = AnalyticsClient.getInstance(this, distictId);
            try {
                mAnalyticsClient.trackProfile(updatedMyProfile);
            } catch (JSONException e) {
                log.error("CANNOT MEET", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mAnalyticsClient != null)
            mAnalyticsClient.flush();
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

    protected void trackGaSetting(EntityManager entityManager) {
        if (entityManager == null) return;
        trackGa(entityManager.getDistictId(), GA_PATH_SETTING);
    }

    protected void trackGaMessageList(EntityManager entityManager, int entityType) {
        if (entityManager == null) return;
        String gaPath = (entityType == JandiConstants.TYPE_CHANNEL) ? GA_PATH_CHANNEL
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
    }
}
