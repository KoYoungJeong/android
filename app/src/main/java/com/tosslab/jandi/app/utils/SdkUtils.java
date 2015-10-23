package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

public class SdkUtils {


    public static boolean isMarshmallow() {
        // Build.VERSION_CODES.M = 23
        return Build.VERSION.SDK_INT >= 23;
    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
