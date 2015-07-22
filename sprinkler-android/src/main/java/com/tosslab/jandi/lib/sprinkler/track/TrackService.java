package com.tosslab.jandi.lib.sprinkler.track;

import android.app.IntentService;
import android.content.Intent;

import com.google.gson.Gson;
import com.tosslab.jandi.lib.sprinkler.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by tonyjs on 15. 7. 22..
 */
public class TrackService extends IntentService {
    public static final String TAG = TrackService.class.getSimpleName();
    public static final String KEY_TRACK = "track";

    public TrackService() {
        super(TAG);
    }

    private TrackDatabaseHelper trackDatabaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        trackDatabaseHelper = TrackDatabaseHelper.getInstance(getApplicationContext());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.d(TAG, ">> track start");
        Serializable passingData = intent.getSerializableExtra(KEY_TRACK);
        if (passingData == null || !(passingData instanceof FutureTrack)) {
            Logger.e(TAG, "You need to passing FutureTrack data.");
            return;
        }

        FutureTrack track = (FutureTrack) passingData;

        String event = track.getEvent();
        String identifiers = mapToJSONFormat(track.getIdentifiersMap());
        String platform = track.getPlatform();
        String properties = mapToJSONFormat(track.getPropertiesMap());
        long time = track.getTime();
        boolean insert = trackDatabaseHelper.insert(event, identifiers, platform, properties, time);
        if (insert) {
            Logger.i(TAG, "FutureTrack insert Success !!!!!!!!!");
        } else {
            Logger.e(TAG, "FutureTrack insert Fail !!!!!!!!!");
        }

        Logger.d(TAG, "<< track end");
    }

    public String mapToJSONFormat(Map<String, ?> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            try {
                jsonObject.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                Logger.e(TAG, "JSONException has occurred.");
                Logger.print(e);
            }
        }
        return jsonObject.toString();
    }

}
