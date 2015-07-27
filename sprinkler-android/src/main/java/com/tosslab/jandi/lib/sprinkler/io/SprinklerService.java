package com.tosslab.jandi.lib.sprinkler.io;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.lib.sprinkler.Logger;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;
import com.tosslab.jandi.lib.sprinkler.io.model.Track;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import retrofit.RetrofitError;

/**
 * Created by tonyjs on 15. 7. 27..
 */
public class SprinklerService extends IntentService {

    public static final String TAG = Logger.makeTag(SprinklerService.class);
    public static final String KEY_TYPE = "type";
    public static final String KEY_TRACK = "track";
    public static final String KEY_DEVICE_ID = "device_id";
    public static final int TYPE_TRACK = 0;
    public static final int TYPE_FLUSH = 1;

    public SprinklerService() {
        super(TAG);
    }

    private Tracker tracker;
    private Flusher flusher;
    private SprinklerUncaughtExceptionHandler exceptionHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        exceptionHandler = new SprinklerUncaughtExceptionHandler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        setUncaughtException();

        int ioType = intent.getIntExtra(KEY_TYPE, TYPE_FLUSH);

        Logger.d(TAG, ">> " + ((ioType == TYPE_TRACK) ? "track" : "flush") + " start ");

        switch (ioType) {
            case TYPE_TRACK:
                track(intent);
                break;
            case TYPE_FLUSH:
                flush(intent);
                break;
        }
    }

    private void track(Intent intent) {
        if (tracker == null) {
            tracker = new Tracker(getApplicationContext());
        }

        Serializable data = intent.getSerializableExtra(KEY_TRACK);
        if (!tracker.validateFutureTrack(data)) {
            Logger.e(TAG, "<< track end(invalidate track)");
            Logger.d(TAG, "<< track end");
            return;
        }

        FutureTrack track = (FutureTrack) data;

        String event = track.getEvent();
        Map<String, Object> identifiers = track.getIdentifiersMap();
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

    private void flush(Intent intent) {
        if (flusher == null) {
            flusher = new Flusher(getApplicationContext());
        }

        String deviceId = intent.getStringExtra(KEY_DEVICE_ID);
        if (TextUtils.isEmpty(deviceId)) {
            Logger.e(TAG, "Need DeviceId to flush.");
            Logger.d(TAG, "<< flush end");
            return;
        }

        Context context = getApplicationContext();
        if (!flusher.availableNetworking(context)) {
            Logger.e(TAG, "Stop flush. Unavailable Networking.");
            Logger.d(TAG, "<< flush end");
            flusher.stopRequest();
            return;
        }

        final Pair<Integer, List<Track>> query = flusher.query();

        if (!flusher.needToFlush(query)) {
            Logger.e(TAG, "Do not need to flush. Track data count is 0.");
            Logger.d(TAG, "<< flush end");
            return;
        }

        int num = query.first;
        final List<Track> data = query.second;
        for (Track track : data) {
            Logger.d(TAG, "track - " + track.getIndex());
        }
        Track firstTrack = data.get(0);
        Track lastTrack = data.get(data.size() - 1);
        long lastDate = lastTrack.getTime();

        int startIndex = firstTrack.getIndex();
        int endIndex = lastTrack.getIndex();

        try {
            ResponseBody response = flusher.flush(num, deviceId, lastDate, data);
            Logger.d(TAG, response.toString());

            if (response.isSuccess()) {
                flusher.deleteRows(startIndex, endIndex);
            }
        } catch (RetrofitError retrofitError) {
            String errorBody = (String) retrofitError.getBodyAs(String.class);
            Logger.e(TAG, "error - " + errorBody);
            Logger.print(retrofitError);
        }

        Logger.d(TAG, "<< flush end");
    }

    private void setUncaughtException() {
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler =
                Thread.currentThread().getUncaughtExceptionHandler();

        if (uncaughtExceptionHandler != null
                && uncaughtExceptionHandler instanceof SprinklerUncaughtExceptionHandler) {
            return;
        }
        Thread.currentThread().setUncaughtExceptionHandler(exceptionHandler);
    }

    @Override
    public void onDestroy() {
        if (flusher != null) {
            flusher.stopRequest();
        }
        super.onDestroy();
    }

    private class SprinklerUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Logger.e(TAG, "Catch the Exception ! \n" + ex.getMessage());
            Logger.print(ex);
            stopSelf();
        }
    }

}
