package com.tosslab.jandi.app.ui;

import android.app.Activity;
import android.content.Intent;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.AnalyticsClient;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.apache.log4j.Logger;
import org.json.JSONException;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class BaseActivity extends Activity {
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

    @Override
    protected void onDestroy() {
        if (mAnalyticsClient != null)
            mAnalyticsClient.flush();
        super.onDestroy();
    }
}
