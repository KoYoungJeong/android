package com.tosslab.jandi.app;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.tosslab.jandi.app.network.JandiNetworkClient;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiException;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;

/**
 * Created by justinygchoi on 2014. 7. 18..
 */
@EActivity
public class SettingsActivity extends PreferenceActivity {
    private final Logger log = Logger.getLogger(SettingsActivity.class);

    @RestService
    TossRestClient tossRestClient;

    private String myToken;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        mContext = getApplicationContext();
        myToken = JandiPreference.getMyToken(mContext);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
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

    /************************************************************
     * Push 설정
     ************************************************************/
    @Background
    public void deleteNotificationTokenInBackground() {
        SharedPreferences prefs = getSharedPreferences(JandiConstants.PREF_NAME_GCM, Context.MODE_PRIVATE);
        String regId = prefs.getString(JandiConstants.PREF_REG_ID, "");

        if (!regId.isEmpty()) {
            JandiNetworkClient jandiNetworkClient = new JandiNetworkClient(tossRestClient, myToken);
            try {
                jandiNetworkClient.deleteNotificationToken(regId);
                log.debug("notification token has been deleted.");
            } catch (JandiException e) {
                log.error("delete notification token failed");
            }
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(JandiConstants.PREF_REG_ID, "");
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
        JandiNetworkClient jandiNetworkClient = new JandiNetworkClient(tossRestClient, myToken);
        try {
            jandiNetworkClient.setNotificationTarget(notificationTarget);
            log.debug("notification target has been changed : " + notificationTarget);
        } catch (JandiException e) {
            log.error("change notification target failed");
            ColoredToast.showError(this, "변환에 실패했습니다");
        }
    }
}