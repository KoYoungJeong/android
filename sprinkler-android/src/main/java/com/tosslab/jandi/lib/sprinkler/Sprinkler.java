package com.tosslab.jandi.lib.sprinkler;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.tosslab.jandi.lib.sprinkler.flush.FlushRetriever;
import com.tosslab.jandi.lib.sprinkler.flush.FlushService;
import com.tosslab.jandi.lib.sprinkler.model.IdentifierKey;
import com.tosslab.jandi.lib.sprinkler.track.factory.SystemTrackFactory;
import com.tosslab.jandi.lib.sprinkler.track.FutureTrack;
import com.tosslab.jandi.lib.sprinkler.track.TrackService;

import java.util.Date;
import java.util.UUID;

/**
 * Created by tonyjs on 15. 7. 20..
 */
public class Sprinkler {
    public static final String TAG = Sprinkler.class.getSimpleName();
    public static boolean IS_DEBUG_MODE = true;

    private static Sprinkler sInstance;
    private Context context;
    private FlushRetriever flushRetriever;
    private Config config;

    private Sprinkler(Context context) {
        this.context = context;
        flushRetriever = new FlushRetriever(context);
        config = new Config(context);

        Logger.d(TAG, config.toString());
    }

    public static Sprinkler with(Context context) {
        if (sInstance == null) {
            sInstance = new Sprinkler(context.getApplicationContext());
        }
        return sInstance;
    }

    public static Sprinkler initialize(Application application, boolean debug) {
        IS_DEBUG_MODE = debug;
        Log.i(TAG, "Sprinkler initialized. debug mode ? " + IS_DEBUG_MODE);
        application.registerActivityLifecycleCallbacks(new LifecycleChecker());
        Context context = application.getApplicationContext();
        return with(context);
    }

    public void startFlushRetriever() {
        flushRetriever.start();
    }

    public boolean isFlushRetrieverStopped() {
        return flushRetriever.isStop();
    }

    public void stopFlushRetriever() {
        flushRetriever.stop();
    }

    /**
     * FutureTrack.Builder 를 이용해 FutureTrack 을 만들어 준다.
     *
     * @param track
     * @return Sprinkler
     */
    public synchronized Sprinkler track(FutureTrack track) {
        if (track == null) {
            Logger.e(TAG, "track fail. You need to passing track.");
            return this;
        }

        if (TextUtils.isEmpty(track.getEvent())) {
            Logger.e(TAG, "track fail. You need to setting track's event.");
            return this;
        }

        track.getIdentifiersMap().put(IdentifierKey.DEVICE_ID, getConfig().getDeviceId());
        track.setPlatform(getConfig().getPlatform());
        track.setTime(new Date().getTime());

        Logger.d(TAG, "track");
        Intent intent = new Intent(context, TrackService.class);
        intent.putExtra(TrackService.KEY_TRACK, track);
        context.startService(intent);
        return this;
    }

    public synchronized Sprinkler flush() {
        Logger.d(TAG, "flush");
        Intent intent = new Intent(context, FlushService.class);
        context.startService(intent);
        return this;
    }

    public void stopAll() {
        track(SystemTrackFactory.getAppCloseTrack());

        stopFlushRetriever();

        Intent intent = new Intent(context, FlushService.class);
        context.stopService(intent);
    }

    public Config getConfig() {
        return config;
    }

}
