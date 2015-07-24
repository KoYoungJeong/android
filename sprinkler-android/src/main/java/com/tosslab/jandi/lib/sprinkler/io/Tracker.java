package com.tosslab.jandi.lib.sprinkler.io;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.lib.sprinkler.Logger;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by tonyjs on 15. 7. 23..
 */
final class Tracker {
    public static final String TAG = Logger.makeTag(Tracker.class);

    private TrackDatabaseHelper databaseHelper;

    public Tracker(Context context) {
        databaseHelper = TrackDatabaseHelper.getInstance(context);
    }

    public boolean validateFutureTrack(Serializable data) {
        if (data == null || !(data instanceof FutureTrack)) {
            Logger.e(TAG, "Invalidate Track Data. You need to passing FutureTrack.");
            return false;
        }

        return true;
    }

    public String mapToJSONFormat(Map<String, ?> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        String toJson = new JSONObject(map).toString();
        return toJson;
    }

    public boolean insert(String event,
                          Map<String, String> identifiersMap,
                          String platform,
                          Map<String, Object> propertiesMap,
                          long time) {
        if (TextUtils.isEmpty(event)) {
            Logger.e(TAG, "Track insert fail. You must be set \'event\' into Track Data.");
            return false;
        }

        String identifiers = mapToJSONFormat(identifiersMap);
        String properties = mapToJSONFormat(propertiesMap);

        boolean insert = databaseHelper.insert(event, identifiers, platform, properties, time);

        return insert;
    }

}
