package com.tosslab.jandi.app;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.utils.ConfigureLog4J;

import org.apache.log4j.Logger;

import java.util.HashMap;

/**
 * Created by justinygchoi on 2014. 6. 19..
 */
public class JandiApplication extends Application {
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
    /**
     * Application의 모든 Activities 가 사용하는 전역 변수
     * Static 등으로 사용하면 LMK에 의해 삭제될 위험이 있음
     */
    private EntityManager mEntityManager = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // For Parse Push Notification
        Parse.initialize(this,
                JandiConstantsForFlavors.PARSE_APPLICATION_ID,
                JandiConstantsForFlavors.PARSE_CLIENT_KEY);

        // For Log4J
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
            analytics.setLocalDispatchPeriod(120);
            Tracker t = analytics.newTracker(JandiConstantsForFlavors.GA_TRACK_ID);

            t.setSessionTimeout(60);
            t.enableAutoActivityTracking(true);
            t.enableExceptionReporting(true);
            t.setAppName("JANDI");
            t.setAppVersion("0.9.1");

            mTrackers.put(trackerId, t);


        }
        return mTrackers.get(trackerId);
    }

    /**
     * *********************************************************
     * Accessors for global
     * **********************************************************
     */
    public EntityManager getEntityManager() {
        return mEntityManager;
    }

    public enum TrackerName {
        APP_TRACKER,
        GLOBAL_TRACKER,
    }

}
