package com.tosslab.jandi.lib.sprinkler;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.tosslab.jandi.lib.sprinkler.trackfactory.SystemTrackFactory;

/**
 * Created by tonyjs on 15. 7. 16..
 */
public class LifecycleChecker implements Application.ActivityLifecycleCallbacks {
    public static final String TAG = Logger.makeTag(LifecycleChecker.class);

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        String activityName = activity.getClass().getName();
        Logger.d(TAG, activityName + " - onActivityCreated");

        Context context = activity.getApplicationContext();
        if (activity.isTaskRoot()) {
            Sprinkler sprinkler = Sprinkler.with(context);
            trackDefaultProperty(sprinkler);
            sprinkler.flush();
            sprinkler.startFlushRetriever();
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        String activityName = activity.getClass().getName();
        Logger.d(TAG, activityName + " - onActivityStarted");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        String activityName = activity.getClass().getName();
        Logger.i(TAG, activityName + " - onActivityResumed");

        Context context = activity.getApplicationContext();

        Sprinkler sprinkler = Sprinkler.with(context);
        if (sprinkler.isFlushRetrieverStopped()) {
            trackDefaultProperty(sprinkler);
            sprinkler.flush();
            sprinkler.startFlushRetriever();
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        String activityName = activity.getClass().getName();
        Logger.w(TAG, activityName + " - onActivityPaused");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        String activityName = activity.getClass().getName();
        Logger.e(TAG, activityName + " - onActivityStopped");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        String activityName = activity.getClass().getName();
        Logger.w(TAG, activityName + " - onActivitySaveInstanceState");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        String activityName = activity.getClass().getName();
        Logger.e(TAG, activityName + " - onActivityDestroyed isTaskRoot ? " + activity.isTaskRoot());
    }

    private void trackDefaultProperty(Sprinkler sprinkler) {
        sprinkler.track(SystemTrackFactory.getAppOpenTrack(sprinkler.getDefaultProperties()));
    }

}
