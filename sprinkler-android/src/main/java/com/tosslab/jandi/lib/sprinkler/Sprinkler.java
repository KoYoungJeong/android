package com.tosslab.jandi.lib.sprinkler;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.lib.sprinkler.io.FlushService;
import com.tosslab.jandi.lib.sprinkler.constant.IdentifierKey;
import com.tosslab.jandi.lib.sprinkler.trackfactory.SystemTrackFactory;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;
import com.tosslab.jandi.lib.sprinkler.io.TrackService;

import java.util.Date;

/**
 * Created by tonyjs on 15. 7. 20..
 */
public class Sprinkler {
    //    public static final String TAG = Sprinkler.class.getSimpleName();
    public static final String TAG = Logger.makeTag(Sprinkler.class);

    public static boolean IS_DEBUG_MODE = true;

    private static Sprinkler sInstance;
    private static final Object sTrackLock = new Object();
    //    private static final Object sFlushLock = new Object();
    private Context context;
    private FlushRetriever flushRetriever;
    private DefaultProperties defaultProperties;

    private Sprinkler(Context context) {
        this.context = context;
        flushRetriever = new FlushRetriever(context);
        defaultProperties = new DefaultProperties(context);

        Logger.d(TAG, defaultProperties.toString());
    }

    public static synchronized Sprinkler with(Context context) {
        if (sInstance == null) {
            sInstance = new Sprinkler(context.getApplicationContext());
        }
        return sInstance;
    }

    public static Sprinkler initialize(Application application, boolean debug) {
        IS_DEBUG_MODE = debug;
        Log.i(TAG, "Sprinkler initialized. debug mode ? " + IS_DEBUG_MODE);
        application.registerActivityLifecycleCallbacks(new LifecycleChecker());
        return with(application.getApplicationContext());
    }

    /**
     * @param track
     * @return Sprinkler
     * @see FutureTrack.Builder 를 이용해 Track 을 만들어 준다.
     */
    public Sprinkler track(FutureTrack track) {
        synchronized (sTrackLock) {
            if (track == null) {
                Logger.e(TAG, "track fail. You need to passing track.");
                return this;
            }

            if (TextUtils.isEmpty(track.getEvent())) {
                Logger.e(TAG, "track fail. You need to setting track's event.");
                return this;
            }

            track.getIdentifiersMap().put(IdentifierKey.DEVICE_ID, getDefaultProperties().getDeviceId());
            track.setPlatform(getDefaultProperties().getPlatform());
            track.setTime(new Date().getTime());

            Logger.d(TAG, "track");
            Intent intent = new Intent(context, TrackService.class);
            intent.putExtra(TrackService.KEY_TRACK, track);
            context.startService(intent);
            return this;
        }
    }

    public Sprinkler flush() {
        Logger.d(TAG, "flush");
        Intent intent = new Intent(context, FlushService.class);
        context.startService(intent);
        return this;
    }

    public void stopFlushService() {
        Intent intent = new Intent(context, FlushService.class);
        intent.putExtra(FlushService.KEY_STOP, true);
        context.startService(intent);
    }

    public void startFlushRetriever() {
        flushRetriever.start();
    }

    public boolean isFlushRetrieverStopped() {
        return flushRetriever.isStopped();
    }

    public void stopFlushRetriever() {
        flushRetriever.stop();
    }

    public void stopAll() {
        track(SystemTrackFactory.getAppCloseTrack());

        flush();

        stopFlushService();

        stopFlushRetriever();
    }

    public DefaultProperties getDefaultProperties() {
        return defaultProperties;
    }

}
