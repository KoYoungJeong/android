package com.tosslab.jandi.lib.sprinkler.tracker;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.lib.sprinkler.domain.Track;

import io.realm.Realm;

/**
 * Created by tonyjs on 15. 7. 8..
 */
public class TrackInputService extends IntentService {
    public static final String TAG = TrackInputService.class.getSimpleName();
    public static final String EXTRA_EVENT = "event";
    public static final String EXTRA_PROPERTY_KEY = "property_key";
    public static final String EXTRA_PROPERTY_VALUE = "property_value";

    public TrackInputService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            Log.e(TAG, "onHandleIntent(\"intent is null value.\")");
            return;
        }

        final String event = intent.getStringExtra(EXTRA_EVENT);
        final String propertyKey = intent.getStringExtra(EXTRA_PROPERTY_KEY);
        final String propertyValue = intent.getStringExtra(EXTRA_PROPERTY_VALUE);

        insert(event, propertyKey, propertyValue);
    }

    private void insert(String event, String propertyKey, String propertyValue) {
        validateExtra(event, EXTRA_EVENT);
        validateExtra(propertyKey, EXTRA_PROPERTY_KEY);
        validateExtra(propertyValue, EXTRA_PROPERTY_VALUE);

        Realm realm = null;
        try {
            realm = Realm.getInstance(getApplicationContext());
            realm.beginTransaction();
            Track track = realm.createObject(Track.class);
            track.setEvent(event);
            track.setPropertyKey(propertyKey);
            track.setPropertyValue(propertyValue);
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    private void validateExtra(String extra, String tag) {
        if (TextUtils.isEmpty(extra)) {
            stopSelf();
            throw new NullPointerException(TAG + " needs \"" + tag +"\"");
        }
    }

}
