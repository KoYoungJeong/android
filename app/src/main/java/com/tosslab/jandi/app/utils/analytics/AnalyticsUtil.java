package com.tosslab.jandi.app.utils.analytics;

import android.os.Bundle;
import android.text.TextUtils;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tosslab.jandi.app.BuildConfig;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.local.orm.repositories.info.SelfRepository;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.io.domain.track.FutureTrack;

public class AnalyticsUtil {

    public static final String FACEBOOK_ACTION = "action";
    public static final String FACEBOOK_LABEL = "label";

    private AnalyticsUtil() {
    }

    public static void sendScreenName(String screenName) {

        if (BuildConfig.DEBUG) return;

        try {
            Tracker tracker = ((JandiApplication) JandiApplication.getContext()).getTracker(JandiApplication.TrackerName.APP_TRACKER);
            tracker.setScreenName(screenName);
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void sendEvent(String category, String action, String label) {
        if (BuildConfig.DEBUG) return;
        try {
            ((JandiApplication) JandiApplication.getContext()).getTracker(JandiApplication.TrackerName.APP_TRACKER)
                    .send(new HitBuilders.EventBuilder()
                            .setCategory(category)
                            .setAction(action)
                            .setLabel(label)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Bundle parameters = new Bundle();
            parameters.putString(FACEBOOK_ACTION, action);
            if (!TextUtils.isEmpty(label)) {
                parameters.putString(FACEBOOK_LABEL, label);
            }
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
        sendEvent(screen.name(), action.name(), null);
    }

    public static void sendEvent(AnalyticsValue.Screen screen, AnalyticsValue.Action action, AnalyticsValue.Label label) {
        sendEvent(screen.name(), action.name(), label.name());
    }

    public static AnalyticsValue.Action getProfileAction(long userId, ShowProfileEvent.From from) {
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
                boolean me = SelfRepository.getInstance().isMe(userId);
                if (me) {
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

    public static void trackSprinkler(FutureTrack futureTrack) {
        Sprinkler sprinkler = Sprinkler.with(JandiApplication.getContext())
                .track(futureTrack);

        if (Sprinkler.IS_DEBUG_MODE) {
            sprinkler.flush();
        }

    }

    public static void flushSprinkler() {
        Sprinkler.with(JandiApplication.getContext())
                .flush();
    }
}
