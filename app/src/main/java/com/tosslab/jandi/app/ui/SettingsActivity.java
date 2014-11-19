package com.tosslab.jandi.app.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.JandiEntityClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;

/**
 * Created by justinygchoi on 2014. 7. 18..
 */
@EActivity
public class SettingsActivity extends PreferenceActivity {
    private final Logger log = Logger.getLogger(SettingsActivity.class);

    @RestService
    JandiRestClient jandiRestClient;
    private JandiEntityClient mJandiEntityClient;

    private String myToken;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpActionBar();

        mContext = getApplicationContext();
        myToken = JandiPreference.getMyToken(mContext);
        mJandiEntityClient = new JandiEntityClient(jandiRestClient, myToken);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
    }

    private void setUpActionBar() {
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

    }

    @Override
    public void onResume() {
        super.onResume();
        trackGa(getDistictId(), "Setting");
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void trackGa(final String distictId, final String gaPath) {
        Tracker screenViewTracker = ((JandiApplication) getApplication())
                .getTracker(JandiApplication.TrackerName.APP_TRACKER);
        screenViewTracker.set("&uid", distictId);
        screenViewTracker.setScreenName(gaPath);
        screenViewTracker.send(new HitBuilders.AppViewBuilder().build());
    }

    private String getDistictId() {
        EntityManager entityManager = ((JandiApplication)getApplication()).getEntityManager();
        return entityManager.getDistictId();
    }

    /************************************************************
     * Push 설정
     ************************************************************/
    @Background
    public void deleteNotificationTokenInBackground() {
        SharedPreferences prefs = getSharedPreferences(JandiConstants.PREF_NAME_GCM, Context.MODE_PRIVATE);
        String regId = prefs.getString(JandiConstants.PREF_PUSH_TOKEN, "");

        if (!regId.isEmpty()) {
            try {
                mJandiEntityClient.deleteNotificationToken(regId);
                log.debug("notification token has been deleted.");
            } catch (JandiNetworkException e) {
                log.error("delete notification token failed");
            }
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(JandiConstants.PREF_PUSH_TOKEN, "");
        editor.commit();

        returnToLoginActivity();
    }

    public void returnToLoginActivity() {
        // Access Token 삭제
        JandiPreference.clearMyToken(mContext);

        Intent intent = new Intent(mContext, IntroActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Background
    public void changeNotificationTarget(String notificationTarget) {
        try {
            mJandiEntityClient.setNotificationTarget(notificationTarget);
            log.debug("notification target has been changed : " + notificationTarget);
        } catch (JandiNetworkException e) {
            log.error("change notification target failed");
            // TODO
            changeNotificationTagerFailed("변환에 실패했습니다");
        }
    }

    @UiThread
    public void changeNotificationTagerFailed(String errMessage) {
        ColoredToast.showError(this, errMessage);
    }
}
