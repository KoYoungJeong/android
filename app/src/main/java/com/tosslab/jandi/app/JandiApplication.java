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
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.utils.ConfigureLog4J;

import org.apache.log4j.Logger;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

/**
 * Created by justinygchoi on 2014. 6. 19..
 */
public class JandiApplication extends Application {
    /**
     * Application의 모든 Activities 가 사용하는 전역 변수
     * Static 등으로 사용하면 LMK에 의해 삭제될 위험이 있음
     */
    private EntityManager mEntityManager = null;

    public enum TrackerName {
        APP_TRACKER,
        GLOBAL_TRACKER,
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

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
            Tracker t = (trackerId == TrackerName.APP_TRACKER)
                    ? analytics.newTracker(JandiConstantsForFlavors.GA_TRACK_ID)
                    : analytics.newTracker(R.xml.global_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }

    /************************************************************
     * Accessors for global
     ************************************************************/
    public EntityManager getEntityManager() {
        return mEntityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        mEntityManager = entityManager;
    }

}
