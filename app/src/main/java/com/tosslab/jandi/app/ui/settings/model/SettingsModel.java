package com.tosslab.jandi.app.ui.settings.model;

import android.content.pm.ActivityInfo;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;

public class SettingsModel {

    public static int getOrientationValue(String value) {

        if (TextUtils.isEmpty(value) || TextUtils.equals(value, "0")) {
            // 자동
            return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        } else if (TextUtils.equals(value, "1")) {
            // 세로 고정
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if (TextUtils.equals(value, "2")) {
            return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    public static int getOrientationSummary(String value) {
        if (TextUtils.isEmpty(value) || TextUtils.equals(value, "0")) {
            // 자동
            return R.string.jandi_screen_auto;
        } else if (TextUtils.equals(value, "1")) {
            // 세로 고정
            return R.string.jandi_screen_vertical;
        } else if (TextUtils.equals(value, "2")) {
            return R.string.jandi_screen_horizontal;
        }
        return R.string.jandi_screen_auto;

    }

}
