package com.tosslab.jandi.app.ui.settings.push;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.views.settings.SettingsBodyCheckView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

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
    private SharedPreferences sharedPreferences;

    @AfterInject
    void initObject() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @AfterViews
    void initViews() {
        boolean pushOn = sharedPreferences.getBoolean("setting_push_auto_alarm", true);

        setUpPushEnabled(pushOn);
    }

    private void setUpPushEnabled(boolean pushOn) {
        sbcvSound.setEnabled(pushOn);
        sbcvLed.setEnabled(pushOn);
        sbcvVibration.setEnabled(pushOn);
    }

    @Click(R.id.vg_settings_push_notification)
    void onNotificationClick() {
        boolean checked = sbcvPush.isChecked();
        setUpPushEnabled(checked);
        setPushOnValue(checked);
    }

    private void setPushOnValue(boolean checked) {

    }

    @Click(R.id.vg_settings_push_sound)
    void onSoundClick() {

    }

    @Click(R.id.vg_settings_push_vibration)
    void onVibrationClick() {

    }

    @Click(R.id.vg_settings_push_led)
    void onLedClick() {

    }


}
