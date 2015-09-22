package com.tosslab.jandi.app.utils.analytics;

import android.os.Bundle;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;

public class AnalyticsUtil {

    public static final String FACEBOOK_ACTION = "action";

    private AnalyticsUtil() {
    }

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

        try {
            Bundle parameters = new Bundle();
            parameters.putString(FACEBOOK_ACTION, action);
            AppEventsLogger.newLogger(JandiApplication.getContext())
                    .logEvent(category, parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void sendScreenName(AnalyticsValue.Screen screen) {
        sendScreenName(screen.name());
    }

    public static void sendEvent(AnalyticsValue.Screen screen, AnalyticsValue.Action action) {
        sendEvent(screen.name(), action.name());
    }

    public static AnalyticsValue.Action getProfileAction(int userId, ShowProfileEvent.From from) {
        AnalyticsValue.Action action;
        switch (from) {
            case Name:
                action = AnalyticsValue.Action.ViewProfile;
                break;
            default:
            case Image:
                action = AnalyticsValue.Action.ViewProfile_Image;
                break;
            case Mention:
                if (userId == EntityManager.getInstance().getMe().getId()) {
                    action = AnalyticsValue.Action.ViewProfile_MyMention;
                } else {
                    action = AnalyticsValue.Action.ViewProfile_Mention;
                }
                break;
            case SystemMessage:
                action = AnalyticsValue.Action.ViewProfile_SysMessage;
                break;
        }
        return action;
    }
}
