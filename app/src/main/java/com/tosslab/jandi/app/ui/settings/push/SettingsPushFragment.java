package com.tosslab.jandi.app.ui.settings.push;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.views.settings.SettingsBodyCheckView;
import com.tosslab.jandi.app.views.settings.SettingsBodyView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.Arrays;

@EFragment(R.layout.fragment_settings_push)
public class SettingsPushFragment extends Fragment {

    @ViewById(R.id.vg_settings_push_notification)
    SettingsBodyCheckView sbcvPush;
    @ViewById(R.id.vg_settings_push_sound)
    SettingsBodyCheckView sbcvSound;
    @ViewById(R.id.vg_settings_push_vibration)
    SettingsBodyCheckView sbcvVibration;
    @ViewById(R.id.vg_settings_push_led)
    SettingsBodyCheckView sbcvLed;
    @ViewById(R.id.vg_settings_push_preview)
    SettingsBodyView sbvPreview;
    private SharedPreferences sharedPreferences;

    @AfterInject
    void initObject() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @AfterViews
    void initViews() {
        boolean pushOn = sharedPreferences.getBoolean("setting_push_auto_alarm", true);
        boolean soundOn = sharedPreferences.getBoolean("setting_push_alarm_sound", true);
        boolean ledOn = sharedPreferences.getBoolean("setting_push_alarm_led", true);
        boolean vibrationOn = sharedPreferences.getBoolean("setting_push_alarm_vibration", true);

        sbcvPush.setChecked(pushOn);
        setPushOnSummary(pushOn);
        sbcvSound.setChecked(soundOn);
        setSoundOnSummary(soundOn);
        sbcvLed.setChecked(ledOn);
        sbcvVibration.setChecked(vibrationOn);

        setUpPushEnabled(pushOn);
        setUpPreview();
    }

    private void setUpPreview() {
        String value = sharedPreferences.getString("setting_push_preview", "0");
        sbvPreview.setSummary(SettingsModel.getPushPreviewSummary(value));
    }

    private void setUpPushEnabled(boolean pushOn) {
        sbcvSound.setEnabled(pushOn);
        sbcvLed.setEnabled(pushOn);
        sbcvVibration.setEnabled(pushOn);
    }

    @Click(R.id.vg_settings_push_notification)
    void onNotificationClick() {
        boolean checked = !sbcvPush.isChecked();
        sbcvPush.setChecked(checked);
        setUpPushEnabled(checked);
        setPushOnValue(checked);
        setPushOnSummary(checked);
    }

    private void setPushOnSummary(boolean checked) {
        sbcvPush.setSummary(checked ?
                R.string.jandi_setting_push_subscription_ok :
                R.string.jandi_setting_push_subscription_cancel);
    }

    private void setPushOnValue(boolean checked) {
        sharedPreferences.edit()
                .putBoolean("setting_push_auto_alarm", checked)
                .apply();
    }

    @Click(R.id.vg_settings_push_sound)
    void onSoundClick() {
        boolean checked = !sbcvSound.isChecked();
        sbcvSound.setChecked(checked);
        setSoundOnValue(checked);
        setSoundOnSummary(checked);
    }

    private void setSoundOnValue(boolean checked) {
        sharedPreferences.edit()
                .putBoolean("setting_push_alarm_sound", true)
                .apply();
    }

    private void setSoundOnSummary(boolean checked) {
        sbcvSound.setSummary(checked ? R.string.jandi_on : R.string.jandi_off);
    }

    @Click(R.id.vg_settings_push_vibration)
    void onVibrationClick() {
        boolean checked = !sbcvVibration.isChecked();
        sbcvVibration.setChecked(checked);
        setVibrationOnValue(checked);
    }

    private void setVibrationOnValue(boolean checked) {
        sharedPreferences.edit()
                .putBoolean("setting_push_alarm_vibration", checked)
                .apply();
    }

    @Click(R.id.vg_settings_push_led)
    void onLedClick() {
        boolean checked = !sbcvLed.isChecked();
        sbcvLed.setChecked(checked);
        setLedOnValue(checked);
    }

    private void setLedOnValue(boolean checked) {
        sharedPreferences.edit()
                .putBoolean("setting_push_alarm_led", checked)
                .apply();
    }


    @Click(R.id.vg_settings_push_preview)
    void onPreviewClick() {
        String value = sharedPreferences.getString("setting_push_preview", "0");
        String[] values = getResources().getStringArray(R.array.jandi_pref_push_preview_values);
        int preselect = Math.max(0, Arrays.asList(values).indexOf(value));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_280)
                .setTitle(R.string.jandi_preview_message_contents)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setSingleChoiceItems(R.array.jandi_pref_push_preview, preselect, (dialog, which) -> {
                    if (which >= 0 && values != null) {
                        String selectedValue = values[which];
                        sharedPreferences.edit()
                                .putString("setting_push_preview", selectedValue)
                                .commit();
                        setUpPreview();
                    }
                    dialog.dismiss();
                });
        builder.create().show();

    }
}
