package com.tosslab.jandi.lib.sprinkler;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

/**
 * Created by tonyjs on 15. 7. 16..
 */
class LifecycleChecker implements Application.ActivityLifecycleCallbacks {
    public static final String TAG = Logger.makeTag(LifecycleChecker.class);

    private int resumed = 0;
    private int stopped = 0;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        String activityName = activity.getClass().getName();
        Logger.d(TAG, activityName + " - onActivityCreated");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        String activityName = activity.getClass().getName();
        Logger.d(TAG, activityName + " - onActivityStarted");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        String activityName = activity.getClass().getName();
        Logger.i(TAG, activityName + " - onActivityResumed");
        Sprinkler sprinkler = Sprinkler.with(activity.getApplicationContext());

        if (resumed == stopped) {
            Logger.i(TAG, "Application On Active.");
            if (sprinkler.isFlushRetrieverStopped()) {
                trackDefaultProperty(sprinkler);
                sprinkler.flush();
                sprinkler.startFlushRetriever();
            }
        }

        resumed++;
        sprinkler.setActive(true);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        String activityName = activity.getClass().getName();
        Logger.w(TAG, activityName + " - onActivityPaused");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        String activityName = activity.getClass().getName();
        Logger.e(TAG, activityName + " - onActivityStopped");

        stopped++;

        if (resumed == stopped) {
            Logger.i(TAG, "Application On Deactive.");
            Sprinkler.with(activity.getApplicationContext()).stopAll();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        String activityName = activity.getClass().getName();
        Logger.w(TAG, activityName + " - onActivitySaveInstanceState");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        String activityName = activity.getClass().getName();
        Logger.e(TAG, activityName + " - onActivityDestroyed");
    }

    private void trackDefaultProperty(Sprinkler sprinkler) {
        DefaultProperties defaultProperties = sprinkler.getDefaultProperties();
        sprinkler.track(new FutureTrack.Builder()
                .event(Event.AppOpen)
                .property(PropertyKey.AppVersion, defaultProperties.getAppVersion())
                .property(PropertyKey.Brand, defaultProperties.getDeviceBrand())
                .property(PropertyKey.Manufacturer, defaultProperties.getDeviceManufacturer())
                .property(PropertyKey.Model, defaultProperties.getDeviceModel())
                .property(PropertyKey.OS, defaultProperties.getOs())
                .property(PropertyKey.OSVersion, defaultProperties.getOsVersion())
                .property(PropertyKey.ScreenDPI, defaultProperties.getScreenDpi())
                .property(PropertyKey.ScreenHeight, defaultProperties.getScreenHeight())
                .property(PropertyKey.ScreenWidth, defaultProperties.getScreenWidth())
                .property(PropertyKey.Carrier, defaultProperties.getDeviceCarrier())
                .property(PropertyKey.Wifi, defaultProperties.isWifiEnabled())
                .build());
    }
}
