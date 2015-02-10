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
        if (TextUtils.equals(preference.getKey(), "setting_push_auto_alarm")) {
            CheckBoxPreference pref = (CheckBoxPreference) preference;
            if (pref.isChecked()) {
                onPushNotification();
            } else {
                offPushNotification();
            }
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
