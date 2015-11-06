package com.tosslab.jandi.app.utils.activity;

import android.app.Activity;

import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.utils.JandiPreference;

public class ActivityHelper {

    public static void setOrientation(Activity activity) {
        String orientation = JandiPreference.getOrientation(activity);
        int orientationValue = SettingsModel.getOrientationValue(orientation);
        activity.setRequestedOrientation(orientationValue);
    }
}
