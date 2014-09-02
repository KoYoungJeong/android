package com.tosslab.jandi.app.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.JandiAuthClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiException;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
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

    private String myToken;
    private Context mContext;

    @Extra
    int myEntityId;
    @Extra
    int myTeamId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        mContext = getApplicationContext();
        myToken = JandiPreference.getMyToken(mContext);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
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

    /************************************************************
     * Push 설정
     ************************************************************/
    @Background
    public void deleteNotificationTokenInBackground() {
        SharedPreferences prefs = getSharedPreferences(JandiConstants.PREF_NAME_GCM, Context.MODE_PRIVATE);
        String regId = prefs.getString(JandiConstants.PREF_REG_ID, "");

        if (!regId.isEmpty()) {
            JandiAuthClient jandiAuthClient = new JandiAuthClient(jandiRestClient);
            jandiAuthClient.setAuthToken(myToken);
            try {
                jandiAuthClient.deleteNotificationToken(regId);
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

        Intent intent = new Intent(mContext, LoginActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Background
    public void changeNotificationTarget(String notificationTarget) {
        JandiAuthClient jandiAuthClient = new JandiAuthClient(jandiRestClient);
        jandiAuthClient.setAuthToken(myToken);
        try {
            jandiAuthClient.setNotificationTarget(notificationTarget);
            log.debug("notification target has been changed : " + notificationTarget);
        } catch (JandiException e) {
            log.error("change notification target failed");
            ColoredToast.showError(this, "변환에 실패했습니다");
        }
    }
}
