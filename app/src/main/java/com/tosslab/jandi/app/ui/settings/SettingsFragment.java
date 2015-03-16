package com.tosslab.jandi.app.ui.settings;/**
 * Created by justinygchoi on 2014. 7. 18..
 */

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.TextUtils;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.SaveCallback;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.settings.viewmodel.SettingFragmentViewModel;
import com.tosslab.jandi.app.ui.term.TermActivity;
import com.tosslab.jandi.app.ui.term.TermActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

@EFragment
public class SettingsFragment extends PreferenceFragment {

    @Bean
    SettingFragmentViewModel settingFragmentViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_setting);

        boolean isPush = ((CheckBoxPreference) getPreferenceManager().findPreference("setting_push_auto_alarm")).isChecked();
        setPushSubState(isPush);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (TextUtils.equals(preference.getKey(), "setting_push_auto_alarm")) {
            CheckBoxPreference pref = (CheckBoxPreference) preference;


            boolean isEnabled;

            if (pref.isChecked()) {
                onPushNotification();
                isEnabled = true;
            } else {
                offPushNotification();
                isEnabled = false;
            }

            setPushSubState(isEnabled);
        } else if (TextUtils.equals(preference.getKey(), "setting_tos")) {

            TermActivity_
                    .intent(getActivity())
                    .termMode(TermActivity.Mode.Agreement.name())
                    .start();

        } else if (TextUtils.equals(preference.getKey(), "setting_pp")) {
            TermActivity_
                    .intent(getActivity())
                    .termMode(TermActivity.Mode.Privacy.name())
                    .start();
        }
        return false;
    }

    private void setPushSubState(boolean isEnabled) {
        Preference soundPref = getPreferenceManager().findPreference("setting_push_alarm_sound");
        Preference ledPref = getPreferenceManager().findPreference("setting_push_alarm_led");
        Preference vibratePref = getPreferenceManager().findPreference("setting_push_alarm_vibration");

        soundPref.setEnabled(isEnabled);
        ledPref.setEnabled(isEnabled);
        vibratePref.setEnabled(isEnabled);
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
