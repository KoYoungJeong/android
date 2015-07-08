package com.tosslab.jandi.lib.sprinkler.tracker;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by tonyjs on 15. 7. 8..
 */
public class TrackOutputService extends IntentService {
    public static final String TAG = TrackOutputService.class.getSimpleName();

    public TrackOutputService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
