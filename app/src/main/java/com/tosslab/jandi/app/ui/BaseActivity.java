package com.tosslab.jandi.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.AnalyticsClient;
import com.tosslab.jandi.app.utils.JandiPreference;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class BaseActivity extends Activity {
    private boolean mDoneAnalizeTrackingSignIn = false;      // 로그인 상황을 MIXPANEL
    private AnalyticsClient mAnalyticsClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mAnalyticsClient = new AnalyticsClient(BaseActivity.this);
    }

    protected void returnToLoginActivity() {
        JandiPreference.clearMyToken(this);
        Intent intent = new Intent(this, LoginActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    protected void trackingSingInByMixpanel(EntityManager entityManager) {
        if (entityManager != null && !mDoneAnalizeTrackingSignIn) {
//            mAnalyticsClient.trackForSingIn(entityManager.getDistictId());
            mDoneAnalizeTrackingSignIn = true;
        }
    }

    @Override
    protected void onDestroy() {
//        mAnalyticsClient.flush();
        super.onDestroy();
    }
}
