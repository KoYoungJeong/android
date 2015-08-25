package com.tosslab.jandi.app.utils.analytics;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tosslab.jandi.app.JandiApplication;

public class GoogleAnalyticsUtil {

    private GoogleAnalyticsUtil() {}

    public static void sendScreenName(String screenName) {
        try {
            Tracker tracker = ((JandiApplication) JandiApplication.getContext()).getTracker(JandiApplication.TrackerName.APP_TRACKER);
            tracker.setScreenName(screenName);
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendEvent(String category, String action) {
        try {
            ((JandiApplication) JandiApplication.getContext()).getTracker(JandiApplication.TrackerName.APP_TRACKER)
                    .send(new HitBuilders.EventBuilder()
                            .setCategory(category)
                            .setAction(action)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
