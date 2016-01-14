package com.tosslab.jandi.app.utils.progresswheel;

import android.app.Activity;

import com.tosslab.jandi.app.utils.ProgressWheel;

import java.lang.ref.WeakReference;

/**
 * Created by tee on 16. 1. 13..
 */
public class ProgressWheelUtil {

    private ProgressWheel progressWheel;

    private ProgressWheelUtil() {
    }

    public static ProgressWheelUtil makeInstance() {
        return new ProgressWheelUtil();
    }

    public void init(Activity activity) {
        progressWheel = new ProgressWheel(activity);
    }

    public void showProgressWheel(Activity activity) {
        if (progressWheel == null) {
            init(activity);
        }
        activity.runOnUiThread(() -> {
            dismissProgressWheel(activity);

        WeakReference<Activity> activityWeakReference = new WeakReference<>(activity);
        progressWheel = new ProgressWheel(activityWeakReference.get());
    }

    public void showProgressWheel(Activity activity) {
        WeakReference<Activity> activityWeakReference = new WeakReference<>(activity);
        if (progressWheel == null) {
            init(activityWeakReference.get());
        }
        activity.runOnUiThread(() -> {
            dismissProgressWheel(activityWeakReference.get());
            if (!progressWheel.isShowing()) {
                progressWheel.show();
            }
        });
    }

    public void dismissProgressWheel(Activity activity) {
        if (progressWheel == null) {
            init(activity);
        WeakReference<Activity> activityWeakReference = new WeakReference<>(activity);
        if (progressWheel == null) {
            init(activityWeakReference.get());
        }
        activity.runOnUiThread(() -> {
            if (progressWheel != null && progressWheel.isShowing()) {
                progressWheel.dismiss();
            }
        });
    }

}
}