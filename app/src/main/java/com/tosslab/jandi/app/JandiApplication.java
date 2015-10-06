package com.tosslab.jandi.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.tosslab.jandi.app.network.SimpleApiRequester;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.manager.apiexecutor.PoolableRequestApiExecutor;
import com.tosslab.jandi.app.network.models.ReqUpdatePlatformStatus;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.utils.ApplicationActivateDetector;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.parse.ParseUpdateUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;

import org.androidannotations.api.BackgroundExecutor;

import java.util.HashMap;
import java.util.concurrent.Executors;

import io.fabric.sdk.android.Fabric;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;

/**
 * Created by justinygchoi on 2014. 6. 19..
 */
public class JandiApplication extends MultiDexApplication {
    static Context context;

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        JandiApplication.context = context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        JandiApplication.setContext(this);
        FacebookSdk.sdkInitialize(this);

        boolean oldParseFileCacheDeleted = JandiPreference.isOldParseFileCacheDeleted(this);
        if (!oldParseFileCacheDeleted) {
            ParseUpdateUtil.removeFileAndCacheIfNeed(this);
            JandiPreference.setOldParseFileCacheDeleted(this, true);
        }

        // For Parse Push Notification
        if (BuildConfig.DEBUG) {
            Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);
        }
        Parse.initialize(this,
                JandiConstantsForFlavors.PARSE_APPLICATION_ID,
                JandiConstantsForFlavors.PARSE_CLIENT_KEY);

        // Set AndroidAnnotations Background pool
        BackgroundExecutor.setExecutor(
                Executors.newScheduledThreadPool(PoolableRequestApiExecutor.MAX_POOL_SIZE));

        Sprinkler.initialize(this, BuildConfig.FLAVOR.contains("dev"), BuildConfig.DEBUG);

        boolean oldParseChannelDeleted = JandiPreference.isOldParseChannelDeleted(this);
        if (!oldParseChannelDeleted) {
            ParseUpdateUtil.refreshChannelOnServer();
            JandiPreference.setOldParseChannelDeleted(this, true);
        }

        registerActivityLifecycleCallbacks();
    }

    private void registerActivityLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(new ApplicationActivateDetector()
                .addActiveListener(() -> updatePlatformStatus(true))
                .addDeactiveListener(() -> updatePlatformStatus(false))
                .addActiveListener(() -> {

                }));
    }

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);

            Tracker tracker = analytics.newTracker(JandiConstantsForFlavors.GA_TRACK_ID);
            tracker.enableAdvertisingIdCollection(true);
            tracker.enableExceptionReporting(true);

            mTrackers.put(trackerId, tracker);

        }
        return mTrackers.get(trackerId);
    }

    public enum TrackerName {
        APP_TRACKER,
        GLOBAL_TRACKER,
    }

    private void updatePlatformStatus(boolean active) {
        LogUtil.i("PlatformApi", "updatePlatformStatus - " + active);

        SimpleApiRequester.request(() -> {
            ReqUpdatePlatformStatus req = new ReqUpdatePlatformStatus(active);
            RequestApiManager.getInstance().updatePlatformStatus(req);
        }, () -> LogUtil.i("PlatformApi", "Success(updatePlatformStatus)"));
    }
}
