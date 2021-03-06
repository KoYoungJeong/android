package com.tosslab.jandi.app.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.tosslab.jandi.app.JandiApplication;

public class SdkUtils {

    public static boolean isOverJellyBeanMR1() {
//         Build.VERSION_CODES.JELLY_BEAN_MR1 = 17
        return Build.VERSION.SDK_INT >= 17;
    }

    public static boolean isOverMarshmallow() {
        // Build.VERSION_CODES.M = 23
        return Build.VERSION.SDK_INT >= 23;
    }

    public static boolean isOverNougat() {
//         Build.VERSION_CODES.N = 24
        return Build.VERSION.SDK_INT >= 24;
    }

    public static boolean hasPermission(String permission) {
        if (isOverMarshmallow()) {
            return ContextCompat.checkSelfPermission(JandiApplication.getContext(), permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public static boolean hasCanvasPermission() {
        if (isOverMarshmallow()) {
            return Settings.canDrawOverlays(JandiApplication.getContext());
        } else {
            return true;
        }
    }

    public static boolean isDeniedPermanently(Activity activity, String permissionString) {
        return !ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionString);
    }
}
