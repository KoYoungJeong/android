package com.tosslab.jandi.app;

import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;

import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

/**
 * Created by justinygchoi on 2014. 6. 19..
 */
public class JandiApplication extends MultiDexApplication {
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        // For Parse Push Notification
        Parse.initialize(this,
                JandiConstantsForFlavors.PARSE_APPLICATION_ID,
                JandiConstantsForFlavors.PARSE_CLIENT_KEY);

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
