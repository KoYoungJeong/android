package com.tosslab.jandi.app;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.tosslab.jandi.app.network.manager.apiexecutor.PoolableRequestApiExecutor;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;

import org.androidannotations.api.BackgroundExecutor;

import java.util.HashMap;
import java.util.concurrent.Executors;

import io.fabric.sdk.android.Fabric;

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

        JandiApplication.setContext(getApplicationContext());

        // For Parse Push Notification
        Parse.initialize(this,
                JandiConstantsForFlavors.PARSE_APPLICATION_ID,
                JandiConstantsForFlavors.PARSE_CLIENT_KEY);

        // Set AndroidAnnotations Background pool
        BackgroundExecutor.setExecutor(
                Executors.newScheduledThreadPool(PoolableRequestApiExecutor.MAX_POOL_SIZE));

        Sprinkler.initialize(this, BuildConfig.DEBUG);
    }

    synchronized public Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER)
                    ? analytics.newTracker(JandiConstantsForFlavors.GA_TRACK_ID)
                    : analytics.newTracker(R.xml.global_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }

    public enum TrackerName {
        APP_TRACKER,
        GLOBAL_TRACKER,
    }

}
