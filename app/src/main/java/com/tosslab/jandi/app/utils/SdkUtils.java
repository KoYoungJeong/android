package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

public class SdkUtils {


    public static boolean isMarshmallow() {
        // Build.VERSION_CODES.M = 23
        return Build.VERSION.SDK_INT >= 23;
    }

    public static boolean hasPermission(Context context, String permission) {
        if (isMarshmallow()) {
            return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }
}
