package com.tosslab.jandi.app.ui;

import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.JsonObject;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.json.JSONException;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class BaseAnalyticsActivity extends AppCompatActivity {
    public static String GA_PATH_CHANNEL = "Topic";
    public static String GA_PATH_DIRECT_MESSAGE = "Direct Message";
    public static String GA_PATH_PRIVATE_GROUP = "Private Group";
    private static String GA_PATH_FILE_DETAIL = "File Detail";
    private static String GA_PATH_PROFILE = "Profile";
    private static String GA_PATH_TEAM_INFO = "Team Info";
    private static String GA_PATH_ACCOUNT_INFO = "Account Info";
    private static String GA_PATH_INVITE_MEMBER = "Invite Member";
    private static String GA_PATH_CHANNEL_PANEL = "Channel Panel";
    private static String GA_PATH_DIRECT_MESSAGE_PANEL = "Direct Message Panel";
    private static String GA_PATH_PRIVATE_GROUP_PANEL = "Private Group Panel";
    private static String GA_PATH_FILE_PANEL = "File Panel";

    private boolean mDoneAnalizeTrackingSignIn = false;      // 로그인 상황을 MIXPANEL
    private MixpanelMemberAnalyticsClient mMixpanelMemberAnalyticsClient;

    protected void returnToIntroStartActivity() {
        finish();
    }

    protected void trackSigningIn(EntityManager entityManager) {
        if (entityManager != null && !mDoneAnalizeTrackingSignIn) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(this, entityManager.getDistictId());
            mMixpanelMemberAnalyticsClient.trackMemberSingingIn();
            mDoneAnalizeTrackingSignIn = true;
        }
    }

    protected void trackSigningInFromPush(EntityManager entityManager) {
        if (entityManager != null) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(this, entityManager.getDistictId());
            mMixpanelMemberAnalyticsClient.trackMemberSingingIn();
        }
    }

    public void trackInvitingToEntity(EntityManager entityManager, int entityType) {
        if (entityManager != null) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mMixpanelMemberAnalyticsClient.trackInvitingToEntity(entityType == JandiConstants.TYPE_PUBLIC_TOPIC);
            } catch (JSONException e) {
                LogUtil.e("CANNOT MEET", e);
            }
        }
    }

    protected void trackDeletingEntity(EntityManager entityManager, int entityType) {
        if (entityManager != null) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mMixpanelMemberAnalyticsClient.trackDeletingEntity(entityType == JandiConstants.TYPE_PUBLIC_TOPIC);
            } catch (JSONException e) {
                LogUtil.e("CANNOT MEET", e);
            }
        }
    }

    protected void trackChangingEntityName(EntityManager entityManager, int entityType) {
        if (entityManager != null) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mMixpanelMemberAnalyticsClient.trackChangingEntityName(entityType == JandiConstants.TYPE_PUBLIC_TOPIC);
            } catch (JSONException e) {
                LogUtil.e("CANNOT MEET", e);
            }
        }
    }

    public void trackLeavingEntity(EntityManager entityManager, int entityType) {
        if (entityManager != null) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mMixpanelMemberAnalyticsClient.trackLeavingEntity(entityType == JandiConstants.TYPE_PUBLIC_TOPIC);
            } catch (JSONException e) {
                LogUtil.e("CANNOT MEET", e);
            }
        }
    }

    protected void trackUploadingFile(EntityManager entityManager,
                                      int entityType, JsonObject fileInfo) {
        if (entityManager != null) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mMixpanelMemberAnalyticsClient.trackUploadingFile(entityType, fileInfo);
            } catch (JSONException e) {
                LogUtil.e("CANNOT MEET", e);
            }
        }
    }

    protected void trackDownloadingFile(EntityManager entityManager, ResMessages.FileMessage fileInfo) {
        if (entityManager != null) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mMixpanelMemberAnalyticsClient.trackDownloadFile(fileInfo);
            } catch (JSONException e) {
                LogUtil.e("CANNOT MEET", e);
            }
        }
    }

    protected void trackSharingFile(EntityManager entityManager,
                                    int entityType, ResMessages.FileMessage fileInfo) {
        if (entityManager != null) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(this, entityManager.getDistictId());
            try {
                mMixpanelMemberAnalyticsClient.trackSharingFile(entityType, fileInfo);
            } catch (JSONException e) {
                LogUtil.e("CANNOT MEET", e);
            }
        }
    }

    protected void trackUnsharingFile(EntityManager entityManager,
                                      int entityType, ResMessages.FileMessage fileInfo) {
        if (entityManager != null) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(this, entityManager.getDistictId());
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
    protected void trackUpdateProfile(String distictId, ResLeftSideMenu.User updatedMyProfile) {
        if (distictId != null) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(this, distictId);
            try {
                mMixpanelMemberAnalyticsClient.trackProfile(updatedMyProfile);
            } catch (JSONException e) {
                LogUtil.e("CANNOT MEET", e);
            }
        }
    }

    protected void trackSignOut(String distictId) {
        if (distictId != null) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(this, distictId);
            mMixpanelMemberAnalyticsClient.trackSignOut();
        }
    }

    protected void trackInviteUser(String distictId) {
        if (distictId != null) {
            mMixpanelMemberAnalyticsClient = MixpanelMemberAnalyticsClient.getInstance(this, distictId);
            mMixpanelMemberAnalyticsClient.trackTeamInvitation();
        }
    }

    @Override
    protected void onDestroy() {
        if (mMixpanelMemberAnalyticsClient != null)
            mMixpanelMemberAnalyticsClient.flush();
        super.onDestroy();
    }


    /**
     * **********************
     * Google Analytics
     */
    protected void trackGaFileDetail(EntityManager entityManager) {
        if (entityManager == null) return;
        trackGa(entityManager.getDistictId(), GA_PATH_FILE_DETAIL);
    }

    protected void trackGaProfile(final String distictId) {
        trackGa(distictId, GA_PATH_PROFILE);
    }

    protected void trackGaTeamInfo(final String districtId) {
        trackGa(districtId, GA_PATH_TEAM_INFO);
    }

    protected void trackGaMessageList(EntityManager entityManager, int entityType) {
        if (entityManager == null) return;
        String gaPath = (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) ? GA_PATH_CHANNEL
                : (entityType == JandiConstants.TYPE_DIRECT_MESSAGE) ? GA_PATH_DIRECT_MESSAGE
                : GA_PATH_PRIVATE_GROUP;
        trackGa(entityManager.getDistictId(), gaPath);
    }

    protected void trackGaAccountInfo(String accountId) {
        trackGa("account_" + accountId, GA_PATH_ACCOUNT_INFO);
    }

    protected void trackGaInviteMember(String distictId) {
        trackGa(distictId, GA_PATH_INVITE_MEMBER);
    }

    protected void trackGaTab(EntityManager entityManager, int tabPosition) {
        if (entityManager == null) {
            return;
        }

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
