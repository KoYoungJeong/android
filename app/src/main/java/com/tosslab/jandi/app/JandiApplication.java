package com.tosslab.jandi.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.decoder.ProgressiveJpegConfig;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.tosslab.jandi.app.local.orm.repositories.AccessTokenRepository;
import com.tosslab.jandi.app.network.SimpleApiRequester;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.manager.apiexecutor.PoolableRequestApiExecutor;
import com.tosslab.jandi.app.network.models.ReqUpdatePlatformStatus;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.utils.ApplicationActivateDetector;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.UnLockPassCodeManager;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.parse.ParseUpdateUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;

import org.androidannotations.api.BackgroundExecutor;

import java.util.HashMap;
import java.util.concurrent.Executors;

import io.fabric.sdk.android.Fabric;

/**
 * Created by justinygchoi on 2014. 6. 19..
 */
public class JandiApplication extends MultiDexApplication {
    public static final String TAG_LIFECYCLE = "Jandi.Lifecycle";

    static Context context;
    static boolean isApplicationDeactive = true;

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        JandiApplication.context = context;
    }

    public static boolean isApplicationDeactive() {
        return isApplicationDeactive;
    }

    public static void setIsApplicationDeactive(boolean isapplicationactive) {
        JandiApplication.isApplicationDeactive = isapplicationactive;
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

        migrationTokenIfNeed();

        registerActivityLifecycleCallbacks();

        registerScreenOffReceiver();

        // Fresco
//        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(context)
//                .setDownsampleEnabled(true)
//                .build();
        Fresco.initialize(context);
    }

    /**
     * Preference 에 토큰정보가 있는 경우 && DB 에 토큰 정보가 없는 경우
     * Preference 값을 DB에 저장
     */
    private void migrationTokenIfNeed() {
        // Preference 에 저장된 정보
        String accessToken = JandiPreference.getAccessToken(this);
        if (TextUtils.isEmpty(accessToken)) {
            return;
        }

        String refreshToken = JandiPreference.getRefreshToken(this);
        String accessTokenType = JandiPreference.getAccessTokenType(this);

        // DB 에 저장된 정보
        ResAccessToken savedAccessToken = AccessTokenRepository.getRepository().getAccessToken();
        String accessTokenFromRepository = savedAccessToken != null ? savedAccessToken.getAccessToken() : null;
        if (TextUtils.isEmpty(accessTokenFromRepository)) {
            ResAccessToken newToken = new ResAccessToken();
            newToken.setAccessToken(accessToken);
            newToken.setRefreshToken(refreshToken);
            newToken.setTokenType(accessTokenType);
            AccessTokenRepository.getRepository().upsertAccessToken(newToken);
        }

        JandiPreference.removeTokenInfo(this);
    }

    private void registerScreenOffReceiver() {
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!JandiApplication.isApplicationDeactive()) {
                    handleApplicationDeactive();
                }
            }
        }, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    private void registerActivityLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(new ApplicationActivateDetector()
                .addActiveListener(() -> updatePlatformStatus(true))
                .addActiveListener(() -> setIsApplicationDeactive(false))
                .addActiveListener(() -> trackApplicationActive()));
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            handleApplicationDeactive();
        }
    }

    private void handleApplicationDeactive() {
        LogUtil.e(TAG_LIFECYCLE, "Deactvie !!");
        setIsApplicationDeactive(true);

        UnLockPassCodeManager.getInstance().setUnLocked(false);
        updatePlatformStatus(false);

        Sprinkler.with(this).stopAll();
    }

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);

            Tracker tracker = analytics.newTracker(JandiConstantsForFlavors.GA_TRACK_ID);
            tracker.enableAdvertisingIdCollection(true);
            tracker.enableExceptionReporting(false);

            mTrackers.put(trackerId, tracker);

        }
        return mTrackers.get(trackerId);
    }

    private void updatePlatformStatus(boolean active) {
        LogUtil.i("PlatformApi", "updatePlatformStatus - " + active);

        ResAccessToken savedAccessToken = AccessTokenRepository.getRepository().getAccessToken();
        String accessToken = savedAccessToken != null ? savedAccessToken.getAccessToken() : null;
        if (TextUtils.isEmpty(accessToken)) {
            return;
        }

        SimpleApiRequester.request(() -> {
            ReqUpdatePlatformStatus req = new ReqUpdatePlatformStatus(active);
            RequestApiManager.getInstance().updatePlatformStatus(req);
        }, () -> LogUtil.i("PlatformApi", "Success(updatePlatformStatus)"));
    }

    private void trackApplicationActive() {
        Sprinkler sprinkler = Sprinkler.with(this);
        if (sprinkler.isFlushRetrieverStopped()) {
            sprinkler.track(sprinkler.getDefaultTrack());
            sprinkler.flush();
            sprinkler.startFlushRetriever();
        }
        sprinkler.setActive(true);
    }

    public enum TrackerName {
        APP_TRACKER,
        GLOBAL_TRACKER,
    }

}
