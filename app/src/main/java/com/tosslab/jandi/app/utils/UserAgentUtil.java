package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.tosslab.jandi.app.JandiApplication;

/**
 * Created by Steve SeongUg Jung on 15. 7. 13..
 */
public class UserAgentUtil {
    public static String getDefaultUserAgent() {
        String osVersion = Build.VERSION.SDK;
        String device = Build.MODEL;
        String appVersion = null;
        try {
            Context context = JandiApplication.getContext();
            appVersion = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            appVersion = "unknown";
            e.printStackTrace();
        }
        return String.format("JandiApp(android; %s; %s; %s;)", osVersion, device, appVersion);
    }
}
