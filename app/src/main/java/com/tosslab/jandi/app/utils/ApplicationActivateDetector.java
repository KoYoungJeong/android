package com.tosslab.jandi.app.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonyjs on 15. 10. 6..
 */
public class ApplicationActivateDetector implements Application.ActivityLifecycleCallbacks {
    public interface OnActiveListener {
        void onActive();
    }

    public interface OnDeactvieListener {
        void onDeactive();
    }

    public static final String TAG = "JANDI.LifecycleCallbacks";
    private int resumed = 0;
    private int stopped = 0;

    private List<OnActiveListener> activeListenerList = new ArrayList<>();
    private List<OnDeactvieListener> deactvieListeners = new ArrayList<>();

    public ApplicationActivateDetector addActiveListener(OnActiveListener listener) {
        activeListenerList.add(listener);
        return this;
    }

    public ApplicationActivateDetector addDeactiveListener(OnDeactvieListener listener) {
        deactvieListeners.add(listener);
        return this;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (resumed == stopped) {
            LogUtil.e(TAG, "resumed == stopped > Active");

            for (OnActiveListener listener : activeListenerList) {
                listener.onActive();
            }
        }

        resumed++;

        LogUtil.i(TAG, "resumed = " + resumed + " stopped = " + stopped);
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        stopped++;

        LogUtil.d(TAG, "resumed = " + resumed + " stopped = " + stopped);

        if (resumed == stopped) {
            LogUtil.e(TAG, "resumed == stopped > Deactive");
            for (OnDeactvieListener listener : deactvieListeners) {
                listener.onDeactive();
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

}
