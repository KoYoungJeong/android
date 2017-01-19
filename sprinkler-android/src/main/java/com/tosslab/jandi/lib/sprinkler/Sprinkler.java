package com.tosslab.jandi.lib.sprinkler;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.lib.sprinkler.constant.DefaultEvent;
import com.tosslab.jandi.lib.sprinkler.constant.DefaultProperties;
import com.tosslab.jandi.lib.sprinkler.constant.DefaultPropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.IdentifierKey;
import com.tosslab.jandi.lib.sprinkler.io.domain.track.FutureTrack;
import com.tosslab.jandi.lib.sprinkler.service.SprinklerService;
import com.tosslab.jandi.lib.sprinkler.util.FlushRetriever;
import com.tosslab.jandi.lib.sprinkler.util.Logger;

import java.util.Date;

/**
 * Created by tonyjs on 15. 7. 20..
 */
public class Sprinkler {
    public static final String TAG = Logger.makeTag(Sprinkler.class);
    public static final String PREFERENCES_NAME = "sprinkler_preferences";
    private static final Object sTrackLock = new Object();
    public static boolean IS_DEBUG_MODE = true;
    public static boolean IS_FOR_DEV = true;
    private static Sprinkler sInstance;
    private Context context;
    private FlushRetriever flushRetriever;
    private DefaultProperties defaultProperties;
    private boolean isActive = false;

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

    public static Sprinkler initialize(Application application, boolean forDev, boolean debugMode) {
        IS_FOR_DEV = forDev;
        IS_DEBUG_MODE = debugMode;
        Log.i(TAG, "Sprinkler initialized. dev version ? " + IS_FOR_DEV);
        Log.i(TAG, "Sprinkler initialized. debug mode ? " + IS_DEBUG_MODE);
        return with(application.getApplicationContext());
    }

    /**
     * @param track (데이터 삽입에 필요한 객체)
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
            track.setVersion(getDefaultProperties().getAppVersion());

            Logger.d(TAG, "track");
            Logger.i(TAG, track.toString());
            Intent intent = new Intent(context, SprinklerService.class);
            intent.putExtra(SprinklerService.KEY_TYPE, SprinklerService.TYPE_TRACK);
            intent.putExtra(SprinklerService.KEY_TRACK, track);
            context.startService(intent);
            return this;
        }
    }

    public Sprinkler flush() {
        Logger.d(TAG, "flush");
        Intent intent = new Intent(context, SprinklerService.class);
        intent.putExtra(SprinklerService.KEY_TYPE, SprinklerService.TYPE_FLUSH);
        intent.putExtra(SprinklerService.KEY_DEVICE_ID, getDefaultProperties().getDeviceId());
        context.startService(intent);
        return this;
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

    public void setActive(boolean active) {
        isActive = active;
    }

    public void stopAll() {
        if (!isActive) {
            return;
        }

        setActive(false);

        track(new FutureTrack.Builder()
                .event(DefaultEvent.AppClose)
                .build());

        flush();

        stopFlushRetriever();
    }

    public DefaultProperties getDefaultProperties() {
        return defaultProperties;
    }

    public FutureTrack getDefaultTrack() {
        return new FutureTrack.Builder()
                .event(DefaultEvent.AppOpen)
                .property(DefaultPropertyKey.AppVersion, defaultProperties.getAppVersion())
                .property(DefaultPropertyKey.Brand, defaultProperties.getDeviceBrand())
                .property(DefaultPropertyKey.Manufacturer, defaultProperties.getDeviceManufacturer())
                .property(DefaultPropertyKey.Model, defaultProperties.getDeviceModel())
                .property(DefaultPropertyKey.OS, defaultProperties.getOs())
                .property(DefaultPropertyKey.OSVersion, defaultProperties.getOsVersion())
                .property(DefaultPropertyKey.ScreenDPI, defaultProperties.getScreenDpi())
                .property(DefaultPropertyKey.ScreenHeight, defaultProperties.getScreenHeight())
                .property(DefaultPropertyKey.ScreenWidth, defaultProperties.getScreenWidth())
                .property(DefaultPropertyKey.Carrier, defaultProperties.getDeviceCarrier())
                .property(DefaultPropertyKey.Wifi, defaultProperties.isWifiEnabled())
                .build();
    }
}
