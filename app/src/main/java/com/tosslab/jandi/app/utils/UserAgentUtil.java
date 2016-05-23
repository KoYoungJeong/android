package com.tosslab.jandi.app.utils;

import android.os.Build;

/**
 * Created by Steve SeongUg Jung on 15. 7. 13..
 */
public class UserAgentUtil {
    public static String getDefaultUserAgent() {
        String osVersion = Build.VERSION.SDK;
        String device = Build.MODEL;
        String appVersion = ApplicationUtil.getAppVersionName();
        return String.format("JandiApp(android; %s; %s; %s;)", osVersion, device, appVersion);
    }
}
