package com.tosslab.jandi.app.ui.settings.model;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
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

    public static int getPushPreviewSummary(String value) {
        if (TextUtils.isEmpty(value) || TextUtils.equals(value, "0")) {
            // 자동
            return R.string.jandi_push_preview_all_message;
        } else if (TextUtils.equals(value, "1")) {
            // 세로 고정
            return R.string.jandi_push_preview_public_only;
        } else if (TextUtils.equals(value, "2")) {
            return R.string.jandi_push_no_preview;
        }
        return R.string.jandi_push_preview_all_message;
    }

    public static String getVersionName() {
        Context context = JandiApplication.getContext();
        String packageName = context.getPackageName();
        try {
            return context.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }
}
