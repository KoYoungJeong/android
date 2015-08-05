package com.tosslab.jandi.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.manager.apiexecutor.PoolableRequestApiExecutor;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.app.network.models.ReqUpdatePlatformStatus;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.network.models.ReqUpdatePlatformStatus;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.api.BackgroundExecutor;

import java.util.HashMap;
import java.util.concurrent.Executors;

import io.fabric.sdk.android.Fabric;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
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

        JandiApplication.setContext(getApplicationContext());

        // For Parse Push Notification
        Parse.initialize(this,
                JandiConstantsForFlavors.PARSE_APPLICATION_ID,
                JandiConstantsForFlavors.PARSE_CLIENT_KEY);

        // Set AndroidAnnotations Background pool
        BackgroundExecutor.setExecutor(
                Executors.newScheduledThreadPool(PoolableRequestApiExecutor.MAX_POOL_SIZE));

        Sprinkler.initialize(this, BuildConfig.DEBUG);

        registerActivityLifecycleCallbacks(new JandiLifecycleCallbacks());
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

    public static final class JandiLifecycleCallbacks implements ActivityLifecycleCallbacks {
        public static final String TAG = "JANDI.LifecycleCallbacks";
        private int resumed = 0;
        private int stopped = 0;

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
            if (resumed == stopped) {
                LogUtil.e(TAG, "resumed == stopped > Active");
                updatePlatformStatus(true);
            }

            resumed++;

            LogUtil.i(TAG, "resumed = " + resumed + " stopped = " + stopped);
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
            stopped++;

            LogUtil.d(TAG, "resumed = " + resumed + " stopped = " + stopped);

            if (resumed == stopped) {
                LogUtil.e(TAG, "resumed == stopped > Deactive");
                updatePlatformStatus(false);
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }

        private void updatePlatformStatus(final boolean active) {
            String accessToken = JandiPreference.getAccessToken(JandiApplication.getContext());
            if (TextUtils.isEmpty(accessToken)) {
                LogUtil.i(TAG, "Don't request(has not accessToken).");
                return;
            }

            Observable.OnSubscribe<ResCommon> updatePlatformStatusSubscribe =
                    subscriber -> {
                        LogUtil.i(TAG, "updatePlatformStatus");
                        try {
                            ReqUpdatePlatformStatus req = new ReqUpdatePlatformStatus(active);
                            RequestApiManager.getInstance().updatePlatformStatus(req);
                            subscriber.onCompleted();
                        } catch (RetrofitError retrofitError) {
                            subscriber.onError(retrofitError);
                        }
                    };

            Observable.create(updatePlatformStatusSubscribe)
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(new Observer<ResCommon>() {
                        @Override
                        public void onCompleted() {
                            LogUtil.e(TAG, "Success(updatePlatformStatus)");
                        }

                        @Override
                        public void onError(Throwable e) {
                            LogUtil.e(TAG, "Error(updatePlatformStatus) - " + e.getMessage());
                        }

                        @Override
                        public void onNext(ResCommon resCommon) {

                        }
                    });
        }
    }
}
