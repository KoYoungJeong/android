package com.tosslab.jandi.app.ui.settings;/**
 * Created by justinygchoi on 2014. 7. 18..
 */

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.SaveCallback;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.settings.viewmodel.SettingFragmentViewModel;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.apache.log4j.Logger;

@EFragment
public class SettingsFragment extends PreferenceFragment {
    private final Logger log = Logger.getLogger(SettingsFragment.class);

    @Bean
    SettingFragmentViewModel settingFragmentViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_setting);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals("setting_push_auto_alarm")) {
            log.debug("setting_push_auto_alarm clicked");
            CheckBoxPreference pref = (CheckBoxPreference) preference;
            if (pref.isChecked()) {
                log.debug("checked");
                onPushNotification();
            } else {
                log.debug("canceled");
                offPushNotification();
            }
        } else if (preference.getKey().equals("setting_logout")) {
            log.debug("setting_logout clicked");
            ColoredToast.show(getActivity(), getString(R.string.jandi_message_logout));

            // Notification Token을 삭제
            settingFragmentViewModel.returnToLoginActivity();
        }
        return false;
    }

    void onPushNotification() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(JandiConstants.PARSE_ACTIVATION, JandiConstants.PARSE_ACTIVATION_ON);
        installation.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                ColoredToast.show(getActivity(), getString(R.string.jandi_setting_push_subscription_ok));
            }
        });
    }

    void offPushNotification() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(JandiConstants.PARSE_ACTIVATION, JandiConstants.PARSE_ACTIVATION_OFF);
        installation.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                ColoredToast.show(getActivity(), getString(R.string.jandi_setting_push_subscription_cancel));
            }
        });
    }
}
