package com.tosslab.jandi.app.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.push.PushPopupActivity;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.lib.sprinkler.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonyjs on 15. 10. 6..
 */
public class ApplicationActivateDetector implements Application.ActivityLifecycleCallbacks {
    public static final String TAG = JandiApplication.TAG_LIFECYCLE;
    private List<OnActiveListener> activeListenerList = new ArrayList<>();

    public ApplicationActivateDetector addActiveListener(OnActiveListener listener) {
        activeListenerList.add(listener);
        return this;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        String activityName = activity.getClass().getName();
        Logger.d(TAG, activityName + " - onActivityCreated");
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

        if (JandiApplication.isApplicationDeactive()) {
            LogUtil.i(TAG, "Active !!");
            for (OnActiveListener listener : activeListenerList) {
                listener.onActive();
            }
        }

        if (!(activity instanceof PushPopupActivity))
            JandiApplication.setIsApplicationDeactive(false);
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
        Logger.e(TAG, activityName + " - onActivityDestroyed");
    }

    public interface OnActiveListener {
        void onActive();
    }

}
