package com.tosslab.jandi.lib.sprinkler.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.lib.sprinkler.io.domain.flush.ResponseBody;
import com.tosslab.jandi.lib.sprinkler.io.flush.Flusher;
import com.tosslab.jandi.lib.sprinkler.io.track.Tracker;
import com.tosslab.jandi.lib.sprinkler.util.Logger;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.io.domain.track.FutureTrack;
import com.tosslab.jandi.lib.sprinkler.io.domain.track.Track;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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

    private static final String PREF_KEY_PRE_REQUEST_FAIL = "pre_request_fail";
    private static final int MAX_ROW_SIZE = 10000;

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
        String version = track.getVersion();

        boolean insert = tracker.insert(event, identifiers, platform, properties, time, version);

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
        // 네트워크 체크
        if (!flusher.availableNetworking(context)) {
            Logger.e(TAG, "Stop flush. Unavailable Networking.");
            Logger.d(TAG, "<< flush end");
            flusher.stopRequest();
            return;
        }

        final Pair<Integer, List<Track>> query = flusher.query();

        // 쌓인 데이터가 하나도 없는 경우
        if (!flusher.needToFlush(query)) {
            Logger.e(TAG, "Do not need to flush. Track data count is 0.");
            Logger.d(TAG, "<< flush end");
            return;
        }

        // 이전 Flush 가 fail 인 경우
        //  - 서버가 살아있는지(ping api 이용) 확인
        //        true > Retry 없이 Flush
        //        fail > 데이터가 10000건 이상 쌓인 경우 첫 데이터로부터 500건 삭제
        boolean isPreRequestFailed = isPreRequestFailed();
        if (isPreRequestFailed && !flusher.isEndPointAlive()) {
            deleteRowsIfNeed();
            return;
        }

        // 이전 Flush 가 fail 인 경우만 Retry
        boolean retry = !isPreRequestFailed;

        flush(retry, query, deviceId);

        Logger.d(TAG, "<< flush end");
    }

    private boolean isPreRequestFailed() {
        SharedPreferences preferences =
                getApplicationContext().getSharedPreferences(
                        Sprinkler.PREFERENCES_NAME, Context.MODE_PRIVATE);

        return preferences.getBoolean(PREF_KEY_PRE_REQUEST_FAIL, false);
    }

    @SuppressLint("CommitPrefEdits")
    private void setPreRequestFailed(boolean failed) {
        SharedPreferences preferences =
                getApplicationContext().getSharedPreferences(
                        Sprinkler.PREFERENCES_NAME, Context.MODE_PRIVATE);

        preferences.edit()
                .putBoolean(PREF_KEY_PRE_REQUEST_FAIL, failed)
                .commit();
    }

    private void deleteRowsIfNeed() {
        int rowCount = flusher.queryForCount();
        if (rowCount < MAX_ROW_SIZE) {
            return;
        }

        flusher.deleteFromBottom();
    }

    private void flush(boolean retry, Pair<Integer, List<Track>> query, String deviceId) {
        int num = query.first;
        final List<Track> data = query.second;
        for (Track track : data) {
            Logger.d(TAG, String.format("track(%d, %s)", track.getIndex(), track.getEvent()));
        }
        Track firstTrack = data.get(0);
        Track lastTrack = data.get(data.size() - 1);
        long lastDate = lastTrack.getTime();
        String lastVersion = lastTrack.getVersion();

        int startIndex = firstTrack.getIndex();
        int endIndex = lastTrack.getIndex();

        try {
            ResponseBody response = flusher.flush(retry, num, deviceId, lastDate, data, lastVersion);
            Logger.d(TAG, response.toString());

            if (response.isSuccess()) {
                Logger.i(TAG, "flush success");
                setPreRequestFailed(false);
                flusher.deleteRows(startIndex, endIndex);
            }
        } catch (Exception e) {
            Logger.e(TAG, "flush fail");
            Logger.print(e);
            setPreRequestFailed(true);
        }
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
