package com.tosslab.jandi.app.ui.settings.push;

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.TextUtils;

import com.parse.ParseInstallation;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.ui.settings.viewmodel.SettingFragmentViewModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.parse.ParseUpdateUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

/**
 * Created by tee on 15. 11. 5..
 */
@EFragment
public class SettingPushFragment extends PreferenceFragment {

    @Bean
    SettingFragmentViewModel settingFragmentViewModel;

    @AfterViews
    void init() {
        settingFragmentViewModel.initProgress(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_push_setting);
        boolean isPush = ((CheckBoxPreference) getPreferenceManager().findPreference("setting_push_auto_alarm")).isChecked();
        setPushSubState(isPush);

        ListPreference settingPushPreview = ((ListPreference) getPreferenceManager().findPreference
                ("setting_push_preview"));
        String value = settingPushPreview.getValue();

        settingPushPreview.setSummary(SettingsModel.getPushPreviewSummary(value));

        settingPushPreview.setOnPreferenceChangeListener((preference, newValue) -> {
            String value1 = newValue.toString();
            preference.setSummary(SettingsModel.getPushPreviewSummary(value1));
            return true;
        });
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

            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, isEnabled ? AnalyticsValue.Action.TurnOnNotifications : AnalyticsValue.Action.TurnOffNotifications);
            setPushSubState(isEnabled);
        } else if (preference.getKey().equals("setting_push_alarm_sound")) {
            CheckBoxPreference pref = (CheckBoxPreference) preference;
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, pref.isChecked() ? AnalyticsValue.Action.SoundsOn : AnalyticsValue.Action.SoundsOff);
        } else if (preference.getKey().equals("setting_push_alarm_vibration")) {
            CheckBoxPreference pref = (CheckBoxPreference) preference;
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, pref.isChecked() ? AnalyticsValue.Action.VibrateOn : AnalyticsValue.Action.VibrateOff);
        } else if (preference.getKey().equals("setting_push_alarm_led")) {
            CheckBoxPreference pref = (CheckBoxPreference) preference;
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.Setting, pref.isChecked() ? AnalyticsValue.Action.PhoneLedOn : AnalyticsValue.Action.PhoneLedOff);
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
        if (installation.containsKey(ParseUpdateUtil.PARSE_ACTIVATION)) {
            String isPushOn = (String) installation.get(ParseUpdateUtil.PARSE_ACTIVATION);
            if (TextUtils.equals(isPushOn, ParseUpdateUtil.PARSE_ACTIVATION_ON)) {
                return;
            }
        }

        installation.put(ParseUpdateUtil.PARSE_ACTIVATION, ParseUpdateUtil.PARSE_ACTIVATION_ON);
        installation.saveEventually(e -> {
            Activity activity = getActivity();
            if (activity != null && !(activity.isFinishing())) {
                ColoredToast.show(activity,
                        activity.getString(R.string.jandi_setting_push_subscription_ok));
            }
        });
    }

    void offPushNotification() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        if (installation.containsKey(ParseUpdateUtil.PARSE_ACTIVATION)) {
            String isPushOff = (String) installation.get(ParseUpdateUtil.PARSE_ACTIVATION);
            if (TextUtils.equals(isPushOff, ParseUpdateUtil.PARSE_ACTIVATION_OFF)) {
                return;
            }
        }

        installation.put(ParseUpdateUtil.PARSE_ACTIVATION, ParseUpdateUtil.PARSE_ACTIVATION_OFF);
        installation.saveEventually(e -> {
            Activity activity = getActivity();
            if (activity != null && !(activity.isFinishing())) {
                ColoredToast.show(activity,
                        activity.getString(R.string.jandi_setting_push_subscription_cancel));
            }
        });
    }

}
