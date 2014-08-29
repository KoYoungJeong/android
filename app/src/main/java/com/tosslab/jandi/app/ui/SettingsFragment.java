package com.tosslab.jandi.app.ui;/**
 * Created by justinygchoi on 2014. 7. 18..
 */

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.EFragment;
import org.apache.log4j.Logger;

@EFragment
public class SettingsFragment extends PreferenceFragment {
    private final Logger log = Logger.getLogger(SettingsFragment.class);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_setting);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals("setting_push_auto_alarm")) {
            log.debug("setting_push_auto_alarm clicked");
            CheckBoxPreference pref = (CheckBoxPreference)preference;
            if (pref.isChecked()) {
                log.debug("checked");
                ((SettingsActivity)getActivity()).changeNotificationTarget(ReqNotificationTarget.TARGET_ALL);
            } else {
                log.debug("canceled");
                ((SettingsActivity)getActivity()).changeNotificationTarget(ReqNotificationTarget.TARGET_NONE);
            }
        } else if (preference.getKey().equals("setting_logout")) {
            log.debug("setting_logout clicked");
            ColoredToast.show(getActivity(), "");

            // Notification Token을 삭제
            ((SettingsActivity)getActivity()).deleteNotificationTokenInBackground();
        }
        return false;
    }
}
