package com.tosslab.jandi.lib.sprinkler.constant;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.tosslab.jandi.lib.sprinkler.Sprinkler;

/**
 * Created by tonyjs on 15. 7. 22..
 */
public class DefaultProperties {
    private static final String PLATFORM = "android";

    private static final String KEY_DEVICE_ID = "device_id";
    private static final String OS = "Android";

    private String deviceId;

    private String platform = PLATFORM;
    private String os = OS;
    private String osVersion = Build.VERSION.RELEASE;
    private String appVersion;
    private String deviceCarrier;
    private String deviceBrand = Build.BRAND;
    private String deviceModel = Build.MODEL;
    private String deviceManufacturer = Build.MANUFACTURER;
    private int screenDpi;
    private int screenWidth;
    private int screenHeight;
    private boolean wifiEnabled = false;

    @SuppressLint("CommitPrefEdits")
    public DefaultProperties(Context context) {
        SharedPreferences pref =
                context.getSharedPreferences(Sprinkler.PREFERENCES_NAME, Context.MODE_PRIVATE);
        String uuid = pref.getString(KEY_DEVICE_ID, null);
        if (TextUtils.isEmpty(uuid)) {
            uuid = JandiApplication.getDeviceUUID();
            pref.edit()
                    .putString(KEY_DEVICE_ID, deviceId)
                    .commit();
        }
        deviceId = uuid;

        try {
            PackageInfo info =
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appVersion = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if (telephonyManager != null) {
            deviceCarrier = telephonyManager.getNetworkOperatorName();
        }

        ConnectivityManager connManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        wifiEnabled = wifiInfo != null && wifiInfo.isAvailable() && wifiInfo.isConnected();

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        screenDpi = metrics.densityDpi;
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }

    public String getPlatform() {
        return platform;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getDeviceCarrier() {
        return deviceCarrier;
    }

    public String getDeviceBrand() {
        return deviceBrand;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }

    public String getOs() {
        return os;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public int getScreenDpi() {
        return screenDpi;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public boolean isWifiEnabled() {
        return wifiEnabled;
    }

    @Override
    public String toString() {
        return "DefaultProperties{" +
                "appVersion='" + appVersion + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", deviceCarrier='" + deviceCarrier + '\'' +
                ", deviceBrand='" + deviceBrand + '\'' +
                ", deviceModel='" + deviceModel + '\'' +
                ", deviceManufacturer='" + deviceManufacturer + '\'' +
                ", os='" + os + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", platform='" + platform + '\'' +
                ", screenDpi=" + screenDpi +
                ", screenWidth=" + screenWidth +
                ", screenHeight=" + screenHeight +
                ", wifiEnabled=" + wifiEnabled +
                '}';
    }
}
