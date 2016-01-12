package com.tosslab.jandi.app.ui.settings.push;

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.parse.ParseInstallation;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.parse.ParseUpdateUtil;

import org.androidannotations.annotations.EFragment;

import java.util.Arrays;

/**
 * Created by tee on 15. 11. 5..
 */
@EFragment
public class SettingPushFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_push_setting);
        boolean isPush = ((CheckBoxPreference) getPreferenceManager().findPreference("setting_push_auto_alarm")).isChecked();
        setPushSubState(isPush);

        Preference settingPushPreview = getPreferenceManager().findPreference("setting_push_preview");
        String value = getPreferenceManager().getSharedPreferences().getString("setting_push_preview", "0");
        settingPushPreview.setSummary(SettingsModel.getPushPreviewSummary(value));
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
        } else if (TextUtils.equals(preference.getKey(), "setting_push_preview")) {
            setUpPushPreview(preference);
        }
        return false;
    }

    private void setUpPushPreview(Preference preference) {
        String value = getPreferenceManager().getSharedPreferences().getString("setting_push_preview", "0");
        String[] values = getResources().getStringArray(R.array.jandi_pref_push_preview_values);
        int preselect = Math.max(0, Arrays.asList(values).indexOf(value));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_280)
                .setTitle(R.string.jandi_preview_message_contents)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setSingleChoiceItems(R.array.jandi_pref_push_preview, preselect, (dialog, which) -> {
                    if (which >= 0 && values != null) {
                        String selectedValue = values[which];
                        getPreferenceManager().getSharedPreferences().edit()
                                .putString("setting_push_preview", selectedValue)
                                .commit();
                        preference.setSummary(SettingsModel.getPushPreviewSummary(selectedValue));
                    }
                    dialog.dismiss();
                });
        builder.create().show();
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
