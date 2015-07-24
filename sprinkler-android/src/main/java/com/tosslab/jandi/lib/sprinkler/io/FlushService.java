package com.tosslab.jandi.lib.sprinkler.io;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import com.tosslab.jandi.lib.sprinkler.Logger;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.io.model.Track;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by tonyjs on 15. 7. 21..
 */
public class FlushService extends IntentService {
    public static final String TAG = Logger.makeTag(FlushService.class);
    public static final String KEY_STOP = "stop";

    public FlushService() {
        super(TAG);
    }

    private Flusher flusher;
    private FlushUncaughtExceptionHandler exceptionHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        flusher = new Flusher(getApplicationContext());
        exceptionHandler = new FlushUncaughtExceptionHandler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.d(TAG, ">> flush start");

        setUncaughtException();

        boolean stop = intent.getBooleanExtra(KEY_STOP, false);
        if (stop) {
            Logger.e(TAG, "stop force !");
            Logger.d(TAG, "<< flush end");
            stopSelf();
            return;
        }

        Context context = getApplicationContext();
        if (!flusher.availableNetworking(context)) {
            Logger.e(TAG, "Stop flush. Unavailable Networking.");
            return;
        }

        final Pair<Integer, List<Track>> query = flusher.query();

        if (!flusher.needToFlush(query)) {
            Logger.e(TAG, "Do not need to flush. Track data count is 0.");
            return;
        }

        int num = query.first;
        String deviceId = Sprinkler.with(context).getDefaultProperties().getDeviceId();
        final List<Track> data = query.second;
        for (Track track : data) {
            Logger.d(TAG, track.toString());
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
                // For Test
                // flusher.deleteRows(startIndex, endIndex);
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
                && uncaughtExceptionHandler instanceof FlushUncaughtExceptionHandler) {
            return;
        }
        Thread.currentThread().setUncaughtExceptionHandler(exceptionHandler);
    }

    private class FlushUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Logger.e(TAG, "Catch the Exception ! \n" + ex.getMessage());
            Logger.print(ex);
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        flusher.stopRequest();
        super.onDestroy();
    }
}
