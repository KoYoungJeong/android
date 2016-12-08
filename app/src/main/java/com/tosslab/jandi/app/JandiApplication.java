package com.tosslab.jandi.app;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tosslab.jandi.app.local.orm.RealmManager;
import com.tosslab.jandi.app.network.SimpleApiRequester;
import com.tosslab.jandi.app.network.client.platform.PlatformApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.apiexecutor.PoolableRequestApiExecutor;
import com.tosslab.jandi.app.network.manager.okhttp.OkHttpClientFactory;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqUpdatePlatformStatus;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceCloser;
import com.tosslab.jandi.app.utils.ApplicationActivateDetector;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UnLockPassCodeManager;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;

import org.androidannotations.api.BackgroundExecutor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.LogManager;

import io.fabric.sdk.android.Fabric;
import io.intercom.android.sdk.Intercom;
import io.intercom.android.sdk.logger.IntercomLogger;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import okhttp3.OkHttpClient;

/**
 * Created by justinygchoi on 2014. 6. 19..
 */
public class JandiApplication extends MultiDexApplication {
    public static final String TAG_LIFECYCLE = "Jandi.Lifecycle";

    static Context context;
    static OkHttpClient okHttpClient;
    static boolean isApplicationDeactive = true;

    Map<TrackerName, Tracker> mTrackers = new HashMap<>();

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        JandiApplication.context = context;
    }

    public static boolean isApplicationDeactive() {
        return isApplicationDeactive;
    }

    public static void setIsApplicationDeactive(boolean isApplicationactive) {
        JandiApplication.isApplicationDeactive = isApplicationactive;
    }

    @NonNull
    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = OkHttpClientFactory.getOkHttpClient();
        }
        return okHttpClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FirebaseAnalytics.getInstance(this);

        addLogConfigIfDebug();

        JandiApplication.setContext(this);
        FacebookSdk.sdkInitialize(this);

        initIntercom();

        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : activityManager.getRunningAppProcesses()) {
            if (Process.myPid() == processInfo.pid) {
                if (TextUtils.equals(processInfo.processName, BuildConfig.APPLICATION_ID)) {
                    if (JandiPreference.getRealmInitiateStamp() < 244) {
                        // 2.5.1.6 이전 버전 사용자, 설치 후 처음 사용자를 대상으로 realm 데이터 초기화
                        Realm.init(this);
                        Realm realm = Realm.getDefaultInstance();
                        RealmConfiguration configuration = realm.getConfiguration();
                        realm.close();
                        Realm.deleteRealm(configuration);

                        JandiPreference.setRealmInitiateStamp();
                    }

                    // proccess 선언이 되어 있지 않은 것에 한해서 동작하도록 함
                    Realm.init(this);
                }
                break;
            }
        }

        RealmManager.init(this);

        StethoInitializer.init(this);

        // Set AndroidAnnotations Background pool
        BackgroundExecutor.setExecutor(
                Executors.newScheduledThreadPool(PoolableRequestApiExecutor.MAX_POOL_SIZE));

        boolean isReleaseBuild =
                BuildConfig.FLAVOR.contains("full") || BuildConfig.FLAVOR.contains("inhouse");
        Sprinkler.initialize(this, !isReleaseBuild, BuildConfig.DEBUG);

        migrationTokenIfNeed();

        registerActivityLifecycleCallbacks();

        registerScreenOffReceiver();

        initRetrofitBuilder();

        logBaidu();
    }

    protected void initIntercom() {

        Intercom.initialize(this, JandiConstantsForFlavors.INTERCOM_API_KEY, JandiConstantsForFlavors.INTERCOM_API_ID);

        if (BuildConfig.DEBUG) {
            Intercom.setLogLevel(IntercomLogger.VERBOSE);
        }
    }

    private void logBaidu() {
//        PushSettings.enableDebugMode(this, LogUtil.LOG);
    }

    private void initRetrofitBuilder() {
        RetrofitBuilder.getInstance();
    }

    private void addLogConfigIfDebug() {
        if (!BuildConfig.DEBUG) return;

        try {

            String config = "handlers=java.util.logging.ConsoleHandler\n" +
                    "java.util.logging.ConsoleHandler.level=ALL\n" +
                    "java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter\n" +
                    ".level=ALL";

            InputStream inputStream = new ByteArrayInputStream(config.getBytes());
            LogManager.getLogManager().readConfiguration(inputStream);
            inputStream.close();
            LogUtil.d("Read Log Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        ResAccessToken savedAccessToken = TokenUtil.getTokenObject();
        String accessTokenFromRepository = savedAccessToken != null ? savedAccessToken.getAccessToken() : null;
        if (TextUtils.isEmpty(accessTokenFromRepository)) {
            ResAccessToken newToken = new ResAccessToken();
            newToken.setAccessToken(accessToken);
            newToken.setRefreshToken(refreshToken);
            newToken.setTokenType(accessTokenType);
            TokenUtil.saveTokenInfoByPassword(newToken);
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

        SocketServiceCloser.getInstance().close();
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

    public static void updatePlatformStatus(boolean active) {
        LogUtil.i("PlatformApi", "updatePlatformStatus - " + active);

        String accessToken = TokenUtil.getAccessToken();
        if (TextUtils.isEmpty(accessToken)) {
            return;
        }

        SimpleApiRequester.request(() -> {
            ReqUpdatePlatformStatus req = new ReqUpdatePlatformStatus(active);
            try {
                new PlatformApi(RetrofitBuilder.getInstance()).updatePlatformStatus(req);
            } catch (RetrofitException e) {
                LogUtil.e("PlatformApi", Log.getStackTraceString(e));
            }
        }, () -> LogUtil.i("PlatformApi", "Success(updatePlatformStatus)"));
    }

    private void trackApplicationActive() {
        Sprinkler sprinkler = Sprinkler.with(this);
        if (sprinkler.isFlushRetrieverStopped()) {
            AnalyticsUtil.trackSprinkler(sprinkler.getDefaultTrack());
            AnalyticsUtil.flushSprinkler();
            sprinkler.startFlushRetriever();
        }
        sprinkler.setActive(true);
    }


    public enum TrackerName {
        APP_TRACKER,
        GLOBAL_TRACKER,
    }

}
