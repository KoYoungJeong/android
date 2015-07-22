package com.tosslab.jandi.lib.sprinkler.flush;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.lib.sprinkler.Logger;
import com.tosslab.jandi.lib.sprinkler.track.Track;
import com.tosslab.jandi.lib.sprinkler.track.TrackDatabaseHelper;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by tonyjs on 15. 7. 21..
 */
public class FlushService extends IntentService {
    public static final String TAG = FlushService.class.getSimpleName();

    public FlushService() {
        super(TAG);
    }

    private TrackDatabaseHelper trackDatabaseHelper;
    private RequestManager requestManager;

    @Override
    public void onCreate() {
        super.onCreate();
        trackDatabaseHelper = TrackDatabaseHelper.getInstance(getApplicationContext());
        requestManager = new RequestManager();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.i(TAG, "onHandleIntent start");
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Logger.i("ExceptionHandler", "Catch the Exception ! \n" + ex.getMessage());
                ex.printStackTrace();
            }
        });

        final Pair<Integer, List<Track>> query = trackDatabaseHelper.query();
        for (Track track : query.second) {
            Logger.d(TAG, track.toString());
        }

        String response = null;
        try {
            response = requestManager.request(getRequest());
        } catch (RetrofitError retrofitError) {
            String errorBody = (String) retrofitError.getBodyAs(String.class);
            Logger.e(TAG, "error - " + errorBody);
            Logger.print(retrofitError);
        }

        if (!TextUtils.isEmpty(response)) {
            Logger.d(TAG, response);
        }

        Logger.i(TAG, "onHandleIntent end");
    }

    private RequestManager.Request<String> getRequest() {
        final SprinklerClient client = requestManager.getClient(SprinklerClient.class);
        return new RequestManager.Request<String>() {
            @Override
            public String performRequest() throws RetrofitError {
                String response = client.post();
                return response;
            }
        };
    }

    @Override
    public void onDestroy() {
        if (requestManager != null) {
            requestManager.stop();
        }
        super.onDestroy();
    }
}
