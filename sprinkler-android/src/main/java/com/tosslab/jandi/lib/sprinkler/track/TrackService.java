package com.tosslab.jandi.lib.sprinkler.track;

import android.app.IntentService;
import android.content.Intent;

import com.tosslab.jandi.lib.sprinkler.Logger;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by tonyjs on 15. 7. 22..
 */
public class TrackService extends IntentService {
    public static final String TAG = Logger.makeTag(TrackService.class);
    public static final String KEY_TRACK = "track";

    public TrackService() {
        super(TAG);
    }

    private Tracker tracker;
    private TrackUncaughtExceptionHandler exceptionHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        tracker = new Tracker(getApplicationContext());
        exceptionHandler = new TrackUncaughtExceptionHandler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.d(TAG, ">> track start");

        setUncaughtException();

        Serializable data = intent.getSerializableExtra(KEY_TRACK);
        if (!tracker.validateFutureTrack(data)) {
            Logger.e(TAG, "<< track end(invalidate track)");
            return;
        }

        FutureTrack track = (FutureTrack) data;

        String event = track.getEvent();
        Map<String, String> identifiers = track.getIdentifiersMap();
        String platform = track.getPlatform();
        Map<String, Object> properties = track.getPropertiesMap();
        long time = track.getTime();

        boolean insert = tracker.insert(event, identifiers, platform, properties, time);

        if (insert) {
            Logger.i(TAG, "Track insert Success.");
        } else {
            Logger.e(TAG, "Track insert Fail.");
        }

        Logger.d(TAG, "<< track end");
    }

    private void setUncaughtException() {
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler =
                Thread.currentThread().getUncaughtExceptionHandler();

        if (uncaughtExceptionHandler != null
                && uncaughtExceptionHandler instanceof TrackUncaughtExceptionHandler) {
            return;
        }
        Thread.currentThread().setUncaughtExceptionHandler(exceptionHandler);
    }

    private class TrackUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Logger.e(TAG, "Catch the Exception ! \n" + ex.getMessage());
            Logger.print(ex);
            stopSelf();
        }
    }

}
