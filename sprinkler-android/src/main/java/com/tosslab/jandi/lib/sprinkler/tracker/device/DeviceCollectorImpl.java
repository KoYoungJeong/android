package com.tosslab.jandi.lib.sprinkler.tracker.device;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.tosslab.jandi.lib.sprinkler.domain.EventProperty;
import com.tosslab.jandi.lib.sprinkler.tracker.keys.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 2..
 */
public class DeviceCollectorImpl implements DeviceCollector {

    public static final String ANDROID = "android";

    @Override
    public List<EventProperty> getDeviceProperties(Context context) {

        List<EventProperty> deviceProperties = new ArrayList<EventProperty>();

        deviceProperties.add(new EventProperty.Builder()
                .propertyName(Property.AppVersion.propertyName())
                .propertyValue(getAppVersion(context))
                .build());

        deviceProperties.add(new EventProperty.Builder()
                .propertyName(Property.Brand.propertyName())
                .propertyValue(getBrandName())
                .build());

        deviceProperties.add(new EventProperty.Builder()
                .propertyName(Property.Manufacture.propertyName())
                .propertyValue(getManufacture())
                .build());

        deviceProperties.add(new EventProperty.Builder()
                .propertyName(Property.Model.propertyName())
                .propertyValue(getModelName())
                .build());

        deviceProperties.add(new EventProperty.Builder()
                .propertyName(Property.OSName.propertyName())
                .propertyValue(ANDROID)
                .build());

        deviceProperties.add(new EventProperty.Builder()
                .propertyName(Property.OSVersion.propertyName())
                .propertyValue(getOsVersion())
                .build());

        deviceProperties.add(new EventProperty.Builder()
                .propertyName(Property.ScreenDPI.propertyName())
                .propertyValue(getScreenDPI(context))
                .build());

        deviceProperties.add(new EventProperty.Builder()
                .propertyName(Property.ScreenHeight.propertyName())
                .propertyValue(getScreenHeight(context))
                .build());

        deviceProperties.add(new EventProperty.Builder()
                .propertyName(Property.ScreenWidth.propertyName())
                .propertyValue(getScreenWidth(context))
                .build());

        return deviceProperties;
    }

    private String getScreenWidth(Context context) {
        return String.valueOf(context.getResources().getDisplayMetrics().widthPixels);
    }

    private String getScreenHeight(Context context) {
        return String.valueOf(context.getResources().getDisplayMetrics().heightPixels);
    }

    private String getScreenDPI(Context context) {
        return String.valueOf(context.getResources().getDisplayMetrics().densityDpi);
    }

    private String getOsVersion() {
        return Build.VERSION.CODENAME;
    }

    private String getModelName() {
        return Build.MODEL;
    }

    private String getManufacture() {
        return Build.BRAND;
    }

    private String getBrandName() {
        return Build.BRAND;
    }

    private String getAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }
}
