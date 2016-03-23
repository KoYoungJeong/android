package com.tosslab.jandi.app.utils.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.utils.JandiPreference;

public class ActivityHelper {

    public static void setOrientation(Activity activity) {
        boolean portraitOnly = activity.getResources().getBoolean(R.bool.portrait_only);
        if (portraitOnly) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            String orientation = JandiPreference.getOrientation(activity);
            int orientationValue = SettingsModel.getOrientationValue(orientation);
            activity.setRequestedOrientation(orientationValue);
        }
    }
}
