package com.tosslab.jandi.app.ui.settings;/**
 * Created by justinygchoi on 2014. 7. 18..
 */

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.MixpanelAnalyticsClient;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.ui.settings.viewmodel.SettingFragmentViewModel;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.apache.log4j.Logger;

@EFragment
public class SettingsFragment extends PreferenceFragment {
    private final Logger log = Logger.getLogger(SettingsFragment.class);

    @Bean
    public SettingFragmentViewModel settingFragmentViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_setting);
    }

    @Override
    public void onResume() {
        super.onResume();
        trackGa(getDistictId(), "Setting");
    }

    private void trackGa(final String distictId, final String gaPath) {
        Tracker screenViewTracker = ((JandiApplication) getActivity().getApplication())
                .getTracker(JandiApplication.TrackerName.APP_TRACKER);
        screenViewTracker.set("&uid", distictId);
        screenViewTracker.setScreenName(gaPath);
        screenViewTracker.send(new HitBuilders.AppViewBuilder().build());
    }

    private String getDistictId() {
        EntityManager entityManager = ((JandiApplication) getActivity().getApplication()).getEntityManager();
        return entityManager.getDistictId();
    }


    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals("setting_push_auto_alarm")) {
            log.debug("setting_push_auto_alarm clicked");
            CheckBoxPreference pref = (CheckBoxPreference) preference;
            if (pref.isChecked()) {
                log.debug("checked");
                settingFragmentViewModel.changeNotificationTarget(ReqNotificationTarget.TARGET_ALL);
            } else {
                log.debug("canceled");
                settingFragmentViewModel.changeNotificationTarget(ReqNotificationTarget.TARGET_NONE);
            }
        } else if (preference.getKey().equals("setting_logout")) {
            log.debug("setting_logout clicked");
            ColoredToast.show(getActivity(), getString(R.string.jandi_message_logout));

            MixpanelAnalyticsClient.getInstance(getActivity(), getDistictId()).trackSignOut();

            // Notification Token을 삭제
            settingFragmentViewModel.returnToLoginActivity();
        }
        return false;
    }
}
