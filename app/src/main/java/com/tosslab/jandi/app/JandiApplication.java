package com.tosslab.jandi.app;

import android.app.Application;
import android.app.DownloadManager;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.koushikdutta.async.http.AsyncSSLEngineConfigurator;
import com.koushikdutta.async.http.socketio.ErrorCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.tosslab.jandi.app.utils.ConfigureLog4J;

import org.apache.log4j.Logger;

import java.util.HashMap;

import javax.net.ssl.SSLEngine;

/**
 * Created by justinygchoi on 2014. 6. 19..
 */
public class JandiApplication extends Application {

    public enum TrackerName {
        APP_TRACKER,
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            ConfigureLog4J.configure(getApplicationContext());

            Logger logger = Logger.getLogger(JandiApplication.class);
            logger.info("initialize log file");
        } catch (Exception e) {
            Log.e("android-log4j", e.getMessage());
        }
    }

    synchronized public Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER)
                    ? analytics.newTracker(getString(R.string.jandi_ga_track_id))
                    : null;
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }
}
